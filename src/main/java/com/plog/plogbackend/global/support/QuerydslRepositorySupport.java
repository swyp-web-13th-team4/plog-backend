package com.plog.plogbackend.global.support;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

public abstract class QuerydslRepositorySupport {

    private Querydsl querydsl;
    private EntityManager entitymanager;
    private JPAQueryFactory jpaQueryFactory;

    private final Class<?> entityClass;


    protected QuerydslRepositorySupport(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @PersistenceContext
    public void setEntitymanager(EntityManager entitymanager) {
        this.entitymanager = entitymanager;

        var entityInformation =
                JpaEntityInformationSupport.getEntityInformation(entityClass, entitymanager);

        EntityPath<?> path =
                SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
        PathBuilder<?> builder = new PathBuilder<>(path.getType(), path.getMetadata());

        this.querydsl = new Querydsl(entitymanager, builder);
        this.jpaQueryFactory = new JPAQueryFactory(entitymanager);
    }

    protected <T> JPAQuery<T> select(EntityPath<T> select) {
        return jpaQueryFactory.select(select);
    }

    protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
        return jpaQueryFactory.selectFrom(from);
    }

    protected <T>JPAQuery<Integer> selectOne(){
        return jpaQueryFactory.selectOne();
    }

    protected <T>JPAUpdateClause update(EntityPath<T> from) {
        return jpaQueryFactory.update(from);
    }

    public void flush() {
        entitymanager.flush();
    }

    public void clear() {
        entitymanager.clear();
    }

}
