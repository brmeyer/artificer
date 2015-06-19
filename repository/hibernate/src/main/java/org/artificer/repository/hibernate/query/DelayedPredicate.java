/*
 * Copyright 2014 JBoss Inc
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

import org.hibernate.ejb.criteria.CriteriaQueryCompiler;
import org.hibernate.ejb.criteria.Renderable;
import org.hibernate.ejb.criteria.predicate.AbstractPredicateImpl;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.Collection;
import java.util.List;

/**
 * Due to the "visitor" nature of ArtificerToHibernateQueryVisitor, most context is not known ahead of time.  Some
 * custom predicates rely on information that might be discovered later on.  So, this class is used as a placeholder.
 * At the very end of processing, the delegate is set.
 *
 * @author Brett Meyer.
 */
public class DelayedPredicate implements Predicate, Renderable {

    private AbstractPredicateImpl delegate;

    public void setDelegate(AbstractPredicateImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public BooleanOperator getOperator() {
        return delegate.getOperator();
    }

    @Override
    public boolean isNegated() {
        return delegate.isNegated();
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
        return delegate.getExpressions();
    }

    @Override
    public Predicate not() {
        return delegate.not();
    }

    @Override
    public Predicate isNull() {
        return delegate.isNull();
    }

    @Override
    public Predicate isNotNull() {
        return delegate.isNotNull();
    }

    @Override
    public Predicate in(Object... values) {
        return delegate.in(values);
    }

    @Override
    public Predicate in(Expression<?>... values) {
        return delegate.in(values);
    }

    @Override
    public Predicate in(Collection<?> values) {
        return delegate.in(values);
    }

    @Override
    public Predicate in(Expression<Collection<?>> values) {
        return delegate.in(values);
    }

    @Override
    public <X> Expression<X> as(Class<X> type) {
        return delegate.as(type);
    }

    @Override
    public Selection<Boolean> alias(String name) {
        return delegate.alias(name);
    }

    @Override
    public boolean isCompoundSelection() {
        return delegate.isCompoundSelection();
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        return delegate.getCompoundSelectionItems();
    }

    @Override
    public Class<? extends Boolean> getJavaType() {
        return delegate.getJavaType();
    }

    @Override
    public String getAlias() {
        return delegate.getAlias();
    }

    @Override
    public String render(CriteriaQueryCompiler.RenderingContext renderingContext) {
        return delegate.render(renderingContext);
    }

    @Override
    public String renderProjection(CriteriaQueryCompiler.RenderingContext renderingContext) {
        return delegate.renderProjection(renderingContext);
    }
}
