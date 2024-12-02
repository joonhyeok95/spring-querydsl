package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslDynamicqueryTest {

    @Autowired
    EntityManager em;
    
    JPAQueryFactory queryFactory;
    
    // Case1. BooleanBuilder
    @Test
    public void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;
        
        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder(); // 여긴 초기값 fix 시 할당
        if(usernameCond != null) {
            builder.and(QMember.member.username.eq(usernameCond));
        }
        if(ageCond != null) {
            builder.and(QMember.member.age.eq(ageCond));
        }
        
        return queryFactory
                .selectFrom(QMember.member)
                .where(builder)
                .fetch();
    }
    
    // Case2. Where 다중 파라미터 사용 (코드가 깔끔)
    @Test
    public void dynamicQuery_WhereParam() {

        String usernameParam = "member1";
        Integer ageParam = 10;
        
        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }
    
    
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {

        return queryFactory
                .selectFrom(QMember.member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression ageEq(Integer ageCond) {
        if(ageCond == null) {
            return null;
        }
        return QMember.member.age.eq(ageCond);
    }

    private BooleanExpression usernameEq(String usernameCond) {
        if(usernameCond == null) {
            return null;
        }
        return QMember.member.username.eq(usernameCond);
//        // 가독성을 생각하면 삼항연산자
//        return usernameCond != null ? QMember.member.username.eq(usernameCond) : null;
    }
    
    // 조건을 조립할 수 있음,
    // ex) 광고 상태 isValid, 날짜가 IN: isServiceable
    private BooleanExpression isServiceable(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }
    
    // querydsl 중급 문법
    @BeforeEach
    @Test
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
