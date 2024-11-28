package study.querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;
//    querydsl 기본생성자가 필요함 NoArgsConstructor로 대체해도 됨
//    public MemberDto() {}
    
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
