package study.querydsl.entity;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class MemberTest {
    @Autowired
    EntityManager em;

    @Test
    public void testEntity() {
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
        
        // 초기화
        em.flush();
        em.clear();
        
        //확인
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        
        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team = " + member.getTeam());
        }
    }
    
    @BeforeEach

    @Test
    public void before() {
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
    
    @Test
    public void startJPQL() { // 런타임 중에 오류가 발생할 수 있어 JPQL의 아쉬운점
        // member1을 찾아라.
        String sql = "select m from Member m where m.username = :username";
        
        Member findMember = em.createQuery(sql, Member.class)
                            .setParameter("username", "member1")
                            .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
        
    }
    JPAQueryFactory queryFactory;
    @Test
    public void startQuerydsl() { // 자바컴파일시점에 오류를 잡을 수 있는 강점.
        queryFactory = new JPAQueryFactory(em); // before 에 넣어도 동시성 문제가 없다.
        //QMember 없으면 Eclipse 기준 [gradle tasks] > [project 선택] > [build폴더] > [build]
//        QMember m = new QMember("m");
        
        Member findMember = queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
}
