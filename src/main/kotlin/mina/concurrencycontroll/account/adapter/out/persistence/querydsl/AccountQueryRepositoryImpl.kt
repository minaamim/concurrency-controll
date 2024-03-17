package mina.concurrencycontroll.account.adapter.out.persistence.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class AccountQueryRepositoryImpl (
    private val jpaQueryFactory: JPAQueryFactory
    ) : AccountQueryRepository{
}