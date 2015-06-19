/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.artificer.repository.hibernate.query;

import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.ArtificerException;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.common.query.xpath.ast.AndExpr;
import org.artificer.common.query.xpath.ast.Argument;
import org.artificer.common.query.xpath.ast.EqualityExpr;
import org.artificer.common.query.xpath.ast.ForwardPropertyStep;
import org.artificer.common.query.xpath.ast.FunctionCall;
import org.artificer.common.query.xpath.ast.LocationPath;
import org.artificer.common.query.xpath.ast.OrExpr;
import org.artificer.common.query.xpath.ast.PrimaryExpr;
import org.artificer.common.query.xpath.ast.Query;
import org.artificer.common.query.xpath.ast.RelationshipPath;
import org.artificer.common.query.xpath.ast.SubartifactSet;
import org.artificer.repository.ClassificationHelper;
import org.artificer.repository.error.QueryExecutionException;
import org.artificer.repository.hibernate.data.HibernateEntityCreator;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerRelationship;
import org.artificer.repository.hibernate.entity.ArtificerTarget;
import org.artificer.repository.hibernate.i18n.Messages;
import org.artificer.repository.query.AbstractArtificerQueryVisitor;
import org.artificer.repository.query.ArtificerQueryArgs;
import org.hibernate.ejb.criteria.predicate.AbstractPredicateImpl;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visitor used to produce a JSQL query from an S-RAMP xpath query.
 *
 * @author Brett Meyer
 */
public class ArtificerToHibernateQueryVisitor extends AbstractArtificerQueryVisitor {

    private final EntityManager entityManager;
    private final FullTextEntityManager fullTextEntityManager;

    private final CriteriaBuilder sqlCriteriaBuilder;
    private final QueryBuilder luceneQueryBuilder;

    private CriteriaQuery sqlQuery = null;
    private From sqlFrom = null;

    private From sqlRelationshipFrom = null;
    private From sqlTargetFrom = null;

    private Subquery sqlCustomPropertySubquery = null;
    private Path sqlCustomPropertyValuePath = null;
    private List<Predicate> sqlCustomPropertyPredicates = null;

    private List<Predicate> sqlPredicates = new ArrayList<>();
    private List<org.apache.lucene.search.Query> lucenePredicates = new ArrayList<>();

    private String propertyContext = null;
    private Object valueContext = null;

    private org.apache.lucene.search.Query fullTextSearch = null;
    private DelayedPredicate fullTextSearchPredicate = null;

    private boolean requiresSQL = false;
    private boolean blockLucenePredicates = false;

    private long totalSize;

    private static final Map<String, String> corePropertyMap = new HashMap<>();
    static {
        corePropertyMap.put("createdBy", "createdBy.username");
        corePropertyMap.put("createdTimestamp", "createdBy.lastActionTime");
        corePropertyMap.put("version", "version");
        corePropertyMap.put("uuid", "uuid");
        corePropertyMap.put("lastModifiedTimestamp", "modifiedBy.lastActionTime");
        corePropertyMap.put("lastModifiedBy", "modifiedBy.username");
        corePropertyMap.put("description", "description");
        corePropertyMap.put("name", "name");
        corePropertyMap.put("contentType", "contentType");
        corePropertyMap.put("contentSize", "contentSize");
        corePropertyMap.put("contentHash", "contentHash");
        corePropertyMap.put("contentEncoding", "contentEncoding");
        corePropertyMap.put("extendedType", "extendedType");
        corePropertyMap.put("derived", "derived");
    }

    private static final Map<String, String> luceneIndexMap = new HashMap<>();
    static {
        luceneIndexMap.put("createdBy", "createdBy.username");
        luceneIndexMap.put("createdTimestamp", "createdBy.lastActionTime");
        luceneIndexMap.put("uuid", "uuid");
        luceneIndexMap.put("lastModifiedTimestamp", "modifiedBy.lastActionTime");
        luceneIndexMap.put("description", "description");
        luceneIndexMap.put("name", "name");
        luceneIndexMap.put("derived", "derived");
    }

    private static final Map<String, String> orderByMap = new HashMap<String, String>();
    static {
        orderByMap.put("createdBy", "createdBy.username");
        orderByMap.put("version", "version");
        orderByMap.put("uuid", "uuid");
        orderByMap.put("createdTimestamp", "createdBy.lastActionTime");
        orderByMap.put("lastModifiedTimestamp", "modifiedBy.lastActionTime");
        orderByMap.put("lastModifiedBy", "modifiedBy.username");
        orderByMap.put("name", "name");
    }

    /**
     * Default constructor.
     * @param entityManager
     * @param classificationHelper
     */
    public ArtificerToHibernateQueryVisitor(EntityManager entityManager, ClassificationHelper classificationHelper) throws ArtificerException {
        super(classificationHelper);

        this.entityManager = entityManager;
        sqlCriteriaBuilder = entityManager.getCriteriaBuilder();

        fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);
        luceneQueryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder()
                .forEntity(ArtificerArtifact.class).get();
    }

    /**
     * Execute the query and return the results.
     * @param args
     * @return List<ArtificerArtifact>
     * @throws ArtificerException
     */
    public List<ArtifactSummary> query(ArtificerQueryArgs args) throws ArtificerException {
        if (this.error != null) {
            throw this.error;
        }

        // If at some point the ability to use indexes was blocked (relationship path, etc.), blow them away.
        if (blockLucenePredicates) {
            lucenePredicates.clear();
        }

        boolean hasLucenePredicates = fullTextSearch != null || lucenePredicates.size() > 0;

        if (!requiresSQL && hasLucenePredicates) {
            // Predicates using Lucene indexes and nothing special requiring SQL.  Rely solely on Lucene.

            FullTextQuery fullTextQuery = doLuceneQuery();
            fullTextQuery.setProjection("uuid", "name", "description", "model", "type", "derived",
                    "createdBy.lastActionTime", "createdBy.username", "modifiedBy.lastActionTime");
            Constructor<ArtifactSummary> summaryCtor;
            try {
                summaryCtor = ArtifactSummary.class.getConstructor(String.class, String.class, String.class,
                        String.class, String.class, Boolean.class, Calendar.class, String.class, Calendar.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            fullTextQuery.setResultTransformer(new AliasToBeanConstructorResultTransformer(summaryCtor));
            List<ArtifactSummary> results = fullTextQuery.getResultList();

            // TODO: Paging?
            totalSize = results.size(); // TODO: temporary

            return results;
        } else if (fullTextSearch != null) {
            // Mix of full-text search and SQL.  Use both.

            FullTextQuery fullTextQuery = doLuceneQuery();
            fullTextQuery.setProjection("id");
            List<Object[]> results = fullTextQuery.getResultList();

            // There is not currently a way to combine JPA Criteria Queries with Hibernate Search Queries.  Until then,
            // we need to build up a list of the full-text result IDs.  That list is then used as a "artifact.id IN ([list])"
            // predicate.
            // Note that some databases (Oracle especially) limit the number of elements in an "in" expression.  Even if
            // it's restricted, they typically allow for at least 1000 elements.  Just to be safe (and maintain
            // portability), break the expressions up into 1000-element chunks.

            List<Predicate> searchResults = new ArrayList<>();
            for (int i = 0; i < results.size(); i += 1000) {
                List<Object[]> subResults;
                if (results.size() > i + 1000) {
                    subResults = results.subList(i, i + 1000 - 1);
                } else {
                    subResults = results;
                }
                Long[] ids = new Long[subResults.size()];
                for (int j = 0; j < subResults.size(); j++) {
                    Object[] result = subResults.get(j);
                    ids[j] = (Long) result[0];
                }
                searchResults.add(sqlFrom.get("id").in(ids));
            }
            if (searchResults.size() > 0) {
                // A "delegate" predicate was set in #fullTextSearch.  This allows us to delay its processing until
                // we know all the possible lucenePredicates.  Otherwise, we wouldn't be able to restrict the Lucene
                // query as much as possible.
                fullTextSearchPredicate.setDelegate((AbstractPredicateImpl) compileOr(searchResults));
            }

            return doSQLQuery(args);
        } else {
            // SQL only.
            return doSQLQuery(args);
        }
    }

    private List<ArtifactSummary> doSQLQuery(ArtificerQueryArgs args) {
        // filter out the trash (have to do this here since 'from' can be overridden at several points in the visitor)
        sqlPredicates.add(sqlCriteriaBuilder.equal(sqlFrom.get("trashed"), Boolean.valueOf(false)));
        // build the full set of constraints and
        sqlQuery.where(compileAnd(sqlPredicates));

        // First, select the total count, without paging
        sqlQuery.select(sqlCriteriaBuilder.count(sqlFrom)).distinct(true);
        totalSize = (Long) entityManager.createQuery(sqlQuery).getSingleResult();

        // Setup the select.  Note that we're only grabbing the fields we need for the summary.
        sqlQuery.multiselect(sqlFrom.get("uuid"), sqlFrom.get("name"), sqlFrom.get("description"), sqlFrom.get("model"), sqlFrom.get("type"), sqlFrom.get("derived"),
                sqlFrom.get("createdBy").get("lastActionTime"), sqlFrom.get("createdBy").get("username"), sqlFrom.get("modifiedBy").get("lastActionTime"))
                .distinct(true);

        if (args.getOrderBy() != null) {
            String propName = orderByMap.get(args.getOrderBy());
            if (propName != null) {
                if (args.getOrderAscending()) {
                    sqlQuery.orderBy(sqlCriteriaBuilder.asc(path(propName)));
                } else {
                    sqlQuery.orderBy(sqlCriteriaBuilder.desc(path(propName)));
                }
            }
        }

        TypedQuery q = entityManager.createQuery(sqlQuery);
        args.applyPaging(q);

        return q.getResultList();
    }

    private FullTextQuery doLuceneQuery() {
        BooleanJunction<BooleanJunction> junction = luceneQueryBuilder.bool();

        // filter out the trash
        junction.must(luceneQueryBuilder.keyword().onField("trashed").matching(false).createQuery());

        if (fullTextSearch != null) {
            // the main full-text query
            junction.must(fullTextSearch);
        }

        if (lucenePredicates.size() > 0) {
            // additional predicates, when available
            for (org.apache.lucene.search.Query lucenePredicate : lucenePredicates) {
                junction.must(lucenePredicate);
            }
        }

        return fullTextEntityManager.createFullTextQuery(junction.createQuery(), ArtificerArtifact.class);
    }

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public void visit(Query node) {
        this.error = null;

        sqlQuery = sqlCriteriaBuilder.createQuery(ArtifactSummary.class);
        sqlFrom = sqlQuery.from(ArtificerArtifact.class);

        node.getArtifactSet().accept(this);
        if (node.getPredicate() != null) {
            node.getPredicate().accept(this);
        }
        if (node.getSubartifactSet() != null) {
            SubartifactSet subartifactSet = node.getSubartifactSet();
            if (subartifactSet.getRelationshipPath() != null) {
                if (subartifactSet.getRelationshipPath().getRelationshipType().equalsIgnoreCase("relatedDocument")) {
                    // derivedFrom
                    sqlFrom = sqlFrom.join("derivedFrom");

                    // Now add any additional predicates included.
                    if (subartifactSet.getPredicate() != null) {
                        subartifactSet.getPredicate().accept(this);
                    }
                } else {
                    // JOIN on the relationship and targets
                    sqlRelationshipFrom = sqlFrom.join("relationships");
                    sqlTargetFrom = sqlRelationshipFrom.join("targets");

                    sqlFrom = sqlRelationshipFrom;

                    // process constraints on the relationship itself
                    subartifactSet.getRelationshipPath().accept(this);

                    // root context now needs to be the relationship targets (permanently)
                    sqlFrom = sqlTargetFrom.join("target");

                    // Now add any additional predicates included.
                    if (subartifactSet.getPredicate() != null) {
                        subartifactSet.getPredicate().accept(this);
                    }
                }

                // since the root context is now based on a relationship target, we can no longer make assumptions about
                // columns that can be offloaded to Lucene
                requiresSQL = true;
                blockLucenePredicates = true;
            }
            if (subartifactSet.getFunctionCall() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_SUBARTIFACTSET_NOT_SUPPORTED"));
            }
            if (subartifactSet.getSubartifactSet() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_TOPLEVEL_SUBARTIFACTSET_ONLY"));
            }
        }
    }

    @Override
    public void visit(LocationPath node) {
        if (node.getArtifactType() != null) {
            // If an explicit type is given, we need to override the root 'from' in order to give the correct entity class.
            // This is so that certain fields can be restricted to their respective entities, rather than cramming
            // all possible fields into ArtificerArtifact itself.
            ArtificerArtifact artifact;
            try {
                artifact = HibernateEntityCreator.visit(ArtifactType.valueOf(node.getArtifactType()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            sqlQuery = sqlCriteriaBuilder.createQuery(ArtifactSummary.class);
            sqlFrom = sqlQuery.from(artifact.getClass());

            eq("type", node.getArtifactType());
        }
        if (node.getArtifactModel() != null) {
            eq("model", node.getArtifactModel());
        }
    }

    @Override
    public void visit(AndExpr node) {
        if (node.getRight() == null) {
            node.getLeft().accept(this);
        } else {
            int lucenePredicateSize = lucenePredicates.size();

            node.getLeft().accept(this);
            node.getRight().accept(this);

            Predicate predicate1 = sqlPredicates.remove(sqlPredicates.size() - 1);
            Predicate predicate2 = sqlPredicates.remove(sqlPredicates.size() - 1);
            sqlPredicates.add(sqlCriteriaBuilder.and(predicate1, predicate2));

            if (lucenePredicates.size() - lucenePredicateSize == 2) {
                // The properties on both sides of the 'and' are indexed by Lucene.
                org.apache.lucene.search.Query query1 = lucenePredicates.remove(lucenePredicates.size() - 1);
                org.apache.lucene.search.Query query2 = lucenePredicates.remove(lucenePredicates.size() - 1);
                lucenePredicates.add(luceneQueryBuilder.bool().must(query1).must(query2).createQuery());
            } else if (lucenePredicates.size() - lucenePredicateSize == 1) {
                // Only one side of the 'and' is indexed by Lucene.  Burn it -- the 'and' will be handled by SQL.
                lucenePredicates.remove(lucenePredicates.size() - 1);
                requiresSQL = true;
            }
        }
    }

    @Override
    public void visit(EqualityExpr node) {

        if (node.getSubartifactSet() != null) {
            node.getSubartifactSet().accept(this);
        } else if (node.getExpr() != null) {
            node.getExpr().accept(this);
        } else if (node.getOperator() == null) {
            node.getLeft().accept(this);
            if (sqlCustomPropertySubquery != null) {
                sqlCustomPropertySubquery.where(compileAnd(sqlCustomPropertyPredicates));
            } else if (propertyContext != null) {
                exists(propertyContext);
            }
        } else {
            node.getLeft().accept(this);
            node.getRight().accept(this);

            if (sqlCustomPropertySubquery != null) {
                sqlCustomPropertyPredicates.add(sqlCriteriaBuilder.equal(sqlCustomPropertyValuePath, valueContext));
                sqlCustomPropertySubquery.where(compileAnd(sqlCustomPropertyPredicates));
            } else if (propertyContext != null) {
                // TODO: Not guaranteed to be propertyContext -- may be function, etc.
                operation(node.getOperator().symbol(), propertyContext, valueContext);
            }

            valueContext = null;
        }
    }

    @Override
    public void visit(ForwardPropertyStep node) {
        if (node.getPropertyQName() != null) {
            String property = node.getPropertyQName().getLocalPart();
            if (corePropertyMap.containsKey(property)) {
                propertyContext = corePropertyMap.get(property);
                sqlCustomPropertySubquery = null;
            } else {
                // Note: Typically, you'd expect to see a really simple MapJoin w/ key and value predicates.
                // However, *negation* ("not()") is needed and is tricky when just using a join.  Instead, use
                // an "a1.id in (select a2.id from ArtificerArtifact a2 [map join and predicates)" -- easily negated.

                sqlCustomPropertySubquery = sqlQuery.subquery(ArtificerArtifact.class);
                From customPropertyFrom = sqlCustomPropertySubquery.from(ArtificerArtifact.class);
                Join customPropertyJoin = customPropertyFrom.join("properties");
                sqlCustomPropertySubquery.select(customPropertyFrom.get("id"));
                sqlCustomPropertyPredicates = new ArrayList<>();
                sqlCustomPropertyPredicates.add(sqlCriteriaBuilder.equal(customPropertyFrom.get("id"), sqlFrom.get("id")));
                sqlCustomPropertyPredicates.add(sqlCriteriaBuilder.equal(customPropertyJoin.get("key"), property));
                sqlCustomPropertyValuePath = customPropertyJoin.get("value");
                sqlPredicates.add(sqlCriteriaBuilder.exists(sqlCustomPropertySubquery));
                propertyContext = null;
                requiresSQL = true;
            }
        }
    }

    @Override
    public void visit(FunctionCall node) {
        if (ArtificerConstants.SRAMP_NS.equals(node.getFunctionName().getNamespaceURI())) {
            if (node.getFunctionName().equals(CLASSIFIED_BY_ALL_OF)) {
                visitClassifications(node, false, true);
            } else if (node.getFunctionName().equals(CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, true, true);
            } else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ALL_OF)) {
                visitClassifications(node, false, false);
            } else if (node.getFunctionName().equals(EXACTLY_CLASSIFIED_BY_ANY_OF)) {
                visitClassifications(node, true, false);
            } else if (node.getFunctionName().equals(GET_RELATIONSHIP_ATTRIBUTE)) {
                String otherAttributeKey = reduceStringLiteralArgument(node.getArguments().get(1));
                // Ex. query: /s-ramp/wsdl/WsdlDocument[someRelationship[s-ramp:getRelationshipAttribute(., 'someAttribute') = 'true']]
                // Note that the predicate function needs to add a condition on the relationship selector itself, *not*
                // the artifact targeted by the relationship.
                sqlCustomPropertySubquery = sqlQuery.subquery(ArtificerRelationship.class);
                From customPropertyFrom = sqlCustomPropertySubquery.from(ArtificerRelationship.class);
                MapJoin customPropertyJoin = customPropertyFrom.joinMap("otherAttributes");
                sqlCustomPropertySubquery.select(customPropertyFrom.get("id"));
                sqlCustomPropertyPredicates = new ArrayList<>();
                sqlCustomPropertyPredicates.add(sqlCriteriaBuilder.equal(customPropertyFrom.get("id"), sqlRelationshipFrom.get("id")));
                sqlCustomPropertyPredicates.add(sqlCriteriaBuilder.equal(customPropertyJoin.key(), otherAttributeKey));
                sqlCustomPropertyValuePath = customPropertyJoin.value();
                sqlPredicates.add(sqlCriteriaBuilder.exists(sqlCustomPropertySubquery));
                propertyContext = null;
                requiresSQL = true;
            } else if (node.getFunctionName().equals(GET_TARGET_ATTRIBUTE)) {
                String otherAttributeKey = reduceStringLiteralArgument(node.getArguments().get(1));
                // Ex. query: /s-ramp/wsdl/WsdlDocument[someRelationship[s-ramp:getTargetAttribute(., 'someAttribute') = 'true']]
                // Note that the predicate function needs to add a condition on the relationship target selector itself, *not*
                // the artifact targeted by the relationship.
                sqlCustomPropertySubquery = sqlQuery.subquery(ArtificerTarget.class);
                From customPropertyFrom = sqlCustomPropertySubquery.from(ArtificerTarget.class);
                MapJoin customPropertyJoin = customPropertyFrom.joinMap("otherAttributes");
                sqlCustomPropertySubquery.select(customPropertyFrom.get("id"));
                sqlCustomPropertyPredicates = new ArrayList<>();
                sqlCustomPropertyPredicates.add(sqlCriteriaBuilder.equal(customPropertyFrom.get("id"), sqlTargetFrom.get("id")));
                sqlCustomPropertyPredicates.add(sqlCriteriaBuilder.equal(customPropertyJoin.key(), otherAttributeKey));
                sqlCustomPropertyValuePath = customPropertyJoin.value();
                sqlPredicates.add(sqlCriteriaBuilder.exists(sqlCustomPropertySubquery));
                propertyContext = null;
                requiresSQL = true;
            } else {
                if (node.getFunctionName().getLocalPart().equals("matches") || node.getFunctionName().getLocalPart().equals("not")) {
                    throw new RuntimeException(Messages.i18n.format("XP_BAD_FUNC_NS", node.getFunctionName().getLocalPart()) );
                }
                throw new RuntimeException(Messages.i18n.format("XP_FUNC_NOT_SUPPORTED", node.getFunctionName().toString()));
            }
        } else if (MATCHES.equals(node.getFunctionName())) {
            if (node.getArguments().size() != 2) {
                throw new RuntimeException(Messages.i18n.format("XP_MATCHES_FUNC_NUM_ARGS_ERROR", node.getArguments().size()));
            }
            Argument attributeArg = node.getArguments().get(0);
            Argument patternArg = node.getArguments().get(1);

            String pattern = reduceStringLiteralArgument(patternArg);

            if (isFullTextSearch(attributeArg)) {
                fullTextSearch(pattern);
            } else {
                pattern = pattern.replace(".*", "%"); // the only valid wildcard
                ForwardPropertyStep attribute = reducePropertyArgument(attributeArg);
                attribute.accept(this);
                like(propertyContext, pattern);
            }
        } else if (NOT.equals(node.getFunctionName())) {
            if (node.getArguments().size() != 1) {
                throw new RuntimeException(Messages.i18n.format("XP_NOT_FUNC_NUM_ARGS_ERROR", node.getArguments().size()));
            }

            Argument argument = node.getArguments().get(0);
            if (argument.getExpr() != null) {
                int lucenePredicateSize = lucenePredicates.size();

                argument.getExpr().accept(this);

                // Should have resulted in only 1 constraint -- negate it and re-add
                Predicate predicate = sqlPredicates.remove(sqlPredicates.size() - 1);
                sqlPredicates.add(sqlCriteriaBuilder.not(predicate));

                if (lucenePredicates.size() > lucenePredicateSize) {
                    // A Lucene predicate was also added.  Negate it.
                    org.apache.lucene.search.Query query = lucenePredicates.remove(lucenePredicates.size() - 1);
                    lucenePredicates.add(luceneQueryBuilder.bool().must(query).not().createQuery());
                }
            } else {
                // TODO: When would not() be given a literal?  That's what this implies.  As-is, it won't be negated...
                argument.accept(this);
            }
        } else {
            throw new RuntimeException(Messages.i18n.format("XP_FUNCTION_NOT_SUPPORTED", node.getFunctionName().toString()));
        }
    }

    private void visitClassifications(FunctionCall node, boolean isOr, boolean allowSubtypes) {
        Collection<URI> classifications = resolveArgumentsToClassifications(node.getArguments());

        Path classifierPath;
        if (allowSubtypes) {
            classifierPath = sqlFrom.get("normalizedClassifiers");
        } else {
            classifierPath = sqlFrom.get("classifiers");
        }

        List<Predicate> classifierConstraints = new ArrayList<>();
        for (URI classification : classifications) {
            classifierConstraints.add(sqlCriteriaBuilder.isMember(classification.toString(), classifierPath));
        }

        if (isOr) {
            sqlPredicates.add(compileOr(classifierConstraints));
        } else {
            sqlPredicates.add(compileAnd(classifierConstraints));
        }

        requiresSQL = true;
    }

    @Override
    public void visit(OrExpr node) {
        if (node.getRight() == null) {
            node.getLeft().accept(this);
        } else {
            int lucenePredicateSize = lucenePredicates.size();

            node.getLeft().accept(this);
            node.getRight().accept(this);

            Predicate predicate1 = sqlPredicates.remove(sqlPredicates.size() - 1);
            Predicate predicate2 = sqlPredicates.remove(sqlPredicates.size() - 1);
            sqlPredicates.add(sqlCriteriaBuilder.or(predicate1, predicate2));

            if (lucenePredicates.size() - lucenePredicateSize == 2) {
                // The properties on both sides of the 'and' are indexed by Lucene.
                org.apache.lucene.search.Query query1 = lucenePredicates.remove(lucenePredicates.size() - 1);
                org.apache.lucene.search.Query query2 = lucenePredicates.remove(lucenePredicates.size() - 1);
                lucenePredicates.add(luceneQueryBuilder.bool().should(query1).should(query2).createQuery());
            } else if (lucenePredicates.size() - lucenePredicateSize == 1) {
                // Only one side of the 'and' is indexed by Lucene.  Burn it -- the and will be handled by SQL.
                lucenePredicates.remove(lucenePredicates.size() - 1);
                requiresSQL = true;
            }
        }
    }

    @Override
    public void visit(PrimaryExpr node) {
        if (node.getLiteral() != null) {
            // If this is a custom property, we must assume that the value will always be a literal String.  If
            // it's a built-in property, correctly handle booleans and timestamps.
            if (sqlCustomPropertySubquery == null) {
                if (propertyContext != null && propertyContext.contains("lastActionTime")) {
                    Date date = null;
                    try {
                        date = SDF.parse(node.getLiteral());
                    } catch (ParseException e) {
                        error = new QueryExecutionException(e);
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    valueContext = calendar;
                } else if ("true".equalsIgnoreCase(node.getLiteral())) {
                    valueContext = Boolean.valueOf(true);
                } else if ("false".equalsIgnoreCase(node.getLiteral())) {
                    valueContext = Boolean.valueOf(false);
                } else {
                    valueContext = node.getLiteral();
                }
            } else {
                valueContext = node.getLiteral();
            }
        } else if (node.getNumber() != null) {
            // TODO: may be an int?
            valueContext = node.getNumber().doubleValue();
        } else if (node.getPropertyQName() != null) {
            throw new RuntimeException(Messages.i18n.format("XP_PROPERTY_PRIMARY_EXPR_NOT_SUPPORTED"));
        }
    }

    @Override
    public void visit(RelationshipPath node) {
        eq("name", node.getRelationshipType());
    }

    @Override
    public void visit(SubartifactSet node) {
        if (node.getFunctionCall() != null) {
            node.getFunctionCall().accept(this);
        } else if (node.getRelationshipPath() != null) {
            From oldRootContext = sqlFrom;

            if (node.getRelationshipPath().getRelationshipType().equalsIgnoreCase("relatedDocument")) {
                // derivedFrom
                sqlFrom = sqlFrom.join("derivedFrom", JoinType.LEFT);

                // Now add any additional predicates included.
                if (node.getPredicate() != null) {
                    node.getPredicate().accept(this);
                }
            } else {
                // Relationship within a predicate.
                // Create a subquery and 'exists' conditional.  The subquery is much easier to handle, later on, if this
                // predicate is negated, as opposed to removing the inner join or messing with left joins.

                List<Predicate> oldSqlPredicates = sqlPredicates;
                sqlPredicates = new ArrayList<>();

                Subquery relationshipSubquery = sqlQuery.subquery(ArtificerRelationship.class);
                sqlRelationshipFrom = relationshipSubquery.from(ArtificerRelationship.class);
                sqlTargetFrom = sqlRelationshipFrom.join("targets");
                relationshipSubquery.select(sqlRelationshipFrom.get("id"));

                Join relationshipOwnerJoin = sqlRelationshipFrom.join("owner");
                sqlPredicates.add(sqlCriteriaBuilder.equal(relationshipOwnerJoin.get("id"), oldRootContext.get("id")));

                sqlFrom = sqlRelationshipFrom;

                // process constraints on the relationship itself
                node.getRelationshipPath().accept(this);

                // context now needs to be the relationship targets

                sqlFrom = sqlTargetFrom.join("target");

                // Now add any additional predicates included.
                if (node.getPredicate() != null) {
                    node.getPredicate().accept(this);
                }

                // Add predicates to subquery
                relationshipSubquery.where(compileAnd(sqlPredicates));

                sqlPredicates = oldSqlPredicates;

                // Add 'exists' predicate (using subquery) to original list
                sqlPredicates.add(sqlCriteriaBuilder.exists(relationshipSubquery));
            }

            // restore the original selector (since the relationship was in a predicate, not a path)
            sqlFrom = oldRootContext;

            requiresSQL = true;

            if (node.getSubartifactSet() != null) {
                throw new RuntimeException(Messages.i18n.format("XP_MULTILEVEL_SUBARTYSETS_NOT_SUPPORTED"));
            }
        }
    }

    private void eq(String propertyName, Object value) {
        if (Boolean.TRUE.equals(value)) {
            sqlPredicates.add(sqlCriteriaBuilder.isTrue(path(propertyName)));
        } else if (Boolean.FALSE.equals(value)) {
            sqlPredicates.add(sqlCriteriaBuilder.isFalse(path(propertyName)));
        } else {
            sqlPredicates.add(sqlCriteriaBuilder.equal(path(propertyName), value));
        }

        if (luceneIndexMap.containsKey(propertyName)) {
            lucenePredicates.add(luceneQueryBuilder.keyword().onField(propertyName).matching(value).createQuery());
        } else {
            requiresSQL = true;
        }
    }

    private void gt(String propertyName, Object value) {
        if (value instanceof Date) {
            sqlPredicates.add(sqlCriteriaBuilder.greaterThan(path(propertyName), (Date) value));
        } else if (value instanceof Calendar) {
            sqlPredicates.add(sqlCriteriaBuilder.greaterThan(path(propertyName), (Calendar) value));
        } else if (value instanceof Number) {
            sqlPredicates.add(sqlCriteriaBuilder.gt(path(propertyName), (Number) value));
        }
        requiresSQL = true;
    }

    private void ge(String propertyName, Object value) {
        if (value instanceof Date) {
            sqlPredicates.add(sqlCriteriaBuilder.greaterThanOrEqualTo(path(propertyName), (Date) value));
        } else if (value instanceof Calendar) {
            sqlPredicates.add(sqlCriteriaBuilder.greaterThanOrEqualTo(path(propertyName), (Calendar) value));
        } else if (value instanceof Number) {
            sqlPredicates.add(sqlCriteriaBuilder.ge(path(propertyName), (Number) value));
        }
        requiresSQL = true;
    }

    private void lt(String propertyName, Object value) {
        if (value instanceof Date) {
            sqlPredicates.add(sqlCriteriaBuilder.lessThan(path(propertyName), (Date) value));
        } else if (value instanceof Calendar) {
            sqlPredicates.add(sqlCriteriaBuilder.lessThan(path(propertyName), (Calendar) value));
        } else if (value instanceof Number) {
            sqlPredicates.add(sqlCriteriaBuilder.lt(path(propertyName), (Number) value));
        }
        requiresSQL = true;
    }

    private void le(String propertyName, Object value) {
        if (value instanceof Date) {
            sqlPredicates.add(sqlCriteriaBuilder.lessThanOrEqualTo(path(propertyName), (Date) value));
        } else if (value instanceof Calendar) {
            sqlPredicates.add(sqlCriteriaBuilder.lessThanOrEqualTo(path(propertyName), (Calendar) value));
        } else if (value instanceof Number) {
            sqlPredicates.add(sqlCriteriaBuilder.le(path(propertyName), (Number) value));
        }
        requiresSQL = true;
    }

    private void like(String propertyName, Object value) {
        sqlPredicates.add(sqlCriteriaBuilder.like(path(propertyName), (String) value));

        if (luceneIndexMap.containsKey(propertyName)) {
            lucenePredicates.add(luceneQueryBuilder.keyword().onField(propertyName).matching(value).createQuery());
        } else {
            requiresSQL = true;
        }
    }

    private void ne(String propertyName, Object value) {
        sqlPredicates.add(sqlCriteriaBuilder.notEqual(path(propertyName), value));

        if (luceneIndexMap.containsKey(propertyName)) {
            org.apache.lucene.search.Query query = luceneQueryBuilder.keyword().onField(propertyName).matching(value).createQuery();
            lucenePredicates.add(luceneQueryBuilder.bool().must(query).not().createQuery());
        } else {
            requiresSQL = true;
        }
    }

    private void operation(String operator, String propertyName, Object value) {
        if ("=".equalsIgnoreCase(operator)) eq(propertyName, value);
        else if (">".equalsIgnoreCase(operator)) gt(propertyName, value);
        else if (">=".equalsIgnoreCase(operator)) ge(propertyName, value);
        else if ("<".equalsIgnoreCase(operator)) lt(propertyName, value);
        else if ("<=".equalsIgnoreCase(operator)) le(propertyName, value);
        else if ("like".equalsIgnoreCase(operator)) like(propertyName, value);
        else if ("<>".equalsIgnoreCase(operator)) ne(propertyName, value);
    }

    private void fullTextSearch(String query) {
        fullTextSearch = luceneQueryBuilder.keyword()
                .onFields("description", "name", "comments.text", "properties.key", "properties.value")
                .andField("content").ignoreFieldBridge()
                .andField("contentPath").ignoreFieldBridge()
                .matching(query)
                .createQuery();
        // Set up a placeholder/delegate predicate.  This will be filled in by #query, after all possible predicates
        // have been collected.
        fullTextSearchPredicate = new DelayedPredicate();
        sqlPredicates.add(fullTextSearchPredicate);
    }

    private void exists(String propertyName) {
        sqlPredicates.add(sqlCriteriaBuilder.isNotNull(path(propertyName)));
        requiresSQL = true;
    }

    private Predicate compileAnd(List<Predicate> constraints) {
        if (constraints.size() == 0) {
            return null;
        } else if (constraints.size() == 1) {
            return constraints.get(0);
        } else {
            return sqlCriteriaBuilder.and(constraints.get(0), compileAnd(constraints.subList(1, constraints.size())));
        }
    }

    private Predicate compileOr(List<Predicate> constraints) {
        if (constraints.size() == 0) {
            return null;
        } else if (constraints.size() == 1) {
            return constraints.get(0);
        } else {
            return sqlCriteriaBuilder.or(constraints.get(0), compileOr(constraints.subList(1, constraints.size())));
        }
    }

    public Path path(String propertyName) {
        if (propertyName.contains(".")) {
            // The propertyName is a path.  Example: createdBy.username, where 'createdBy' is an @Embedded User.
            // Needs to become sqlFrom.get("createdBy").get("username").
            String[] split = propertyName.split("\\.");
            Path path = sqlFrom.get(split[0]);
            if (split.length > 1) {
                for (int i = 1; i < split.length; i++) {
                    path = path.get(split[i]);
                }
            }
            return path;
        } else {
            return sqlFrom.get(propertyName);
        }
    }

}