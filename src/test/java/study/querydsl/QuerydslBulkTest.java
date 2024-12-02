package study.querydsl;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
//@Rollback(value = false)
public class QuerydslBulkTest {

    @Autowired
    EntityManager em;
    
    JPAQueryFactory queryFactory;
    
    // Case1. BooleanBuilder
    @Test
    public void bulkUpdate() {
        // member1 = 10 -> 비회원
        // member2 = 20 -> 비회원
        // member3 = 30 -> 유지
        // member4 = 40 -> 유지
       long count = queryFactory
               .update(QMember.member)
               .set(QMember.member.username, "비회원")
               .where(QMember.member.age.lt(28))
               .execute();
       // 벌크는 항상 영속성컨텍스트를 날려줘야하는 걸 잊지말자
       em.flush();
       em.clear();
       
       List<Member> result = queryFactory
               .selectFrom(QMember.member)
               .fetch();
       
       for (Member member : result) {
           System.out.println("MEMBER -> " + member);
       }
    }

    @Test
    public void bulkAdd() {
        long count = queryFactory
                .update(QMember.member)
                .set(QMember.member.age, QMember.member.age.multiply(2))
                .execute();
    }

    @Test
    public void bulkDelete() {
        long count = queryFactory
                .delete(QMember.member)
                .where(QMember.member.age.gt(18))
                .execute();
    }
    

    // querydsl 중급 문법
    @BeforeEach
//    @Test
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

}
