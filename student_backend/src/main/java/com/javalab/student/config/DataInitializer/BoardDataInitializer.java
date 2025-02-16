/*
package com.javalab.student.config.DataInitializer;

import com.javalab.student.entity.board.Board;
import com.javalab.student.entity.Member;
import com.javalab.student.entity.board.Post;
import com.javalab.student.repository.board.BoardRepository;
import com.javalab.student.repository.MemberRepository;
import com.javalab.student.repository.board.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BoardDataInitializer implements CommandLineRunner {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeBoards();
        initializePosts();
    }

    private void initializeBoards() {
        if (boardRepository.count() > 0) {
            System.out.println("✅ Board 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        Board noticeBoard = Board.builder().name("공지사항").description("필루션 공지사항 게시판입니다.").build();
        Board faqBoard = Board.builder().name("자주 묻는 질문").description("자주 묻는 질문을 정리한 게시판입니다.").build();

        boardRepository.saveAll(List.of(noticeBoard, faqBoard));
        System.out.println("✅ Board 데이터 초기화 완료");
    }

    private void initializePosts() {
        if (postRepository.count() > 0) {
            System.out.println("✅ Post 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        Optional<Member> adminMember = memberRepository.findById(1L);
        if (adminMember.isEmpty()) {
            System.out.println("❌ 관리자 회원이 존재하지 않습니다. 초기화를 중단합니다.");
            return;
        }

        List<Board> boards = boardRepository.findAll();
        Board noticeBoard = boards.stream().filter(b -> b.getName().equals("공지사항")).findFirst().orElseThrow();
        Board faqBoard = boards.stream().filter(b -> b.getName().equals("자주 묻는 질문")).findFirst().orElseThrow();

        List<Post> posts = List.of(
                // ✅ 공지사항 4개 하드코딩 함 ㅠㅠ
                createPost(adminMember.get().getId(), noticeBoard, "필루션 이용약관 및 개인정보처리방침 변경 안내",
                        "안녕하세요.\n필루션 고객님에게 깊은 감사를 드립니다.\n\n보다 나은 서비스를 제공하고 명확한 이용을 위해 필루션 이용약관과 개인정보처리방침이 개정 되었습니다.﻿\n\n"
                                + "서비스 이용에 참고하여 주시기 바랍니다.\n\n"
                                + "1. 주요 내용\n\n"
                                + "- 서비스이용약관\n"
                                + "  : 적립금 제도 운영 조항 추가\n"
                                + "  : 개인정보처리방침의 개정법 적용에 따른 수정\n"
                                + "- 개인정보처리방침\n"
                                + "  : 개인정보보호법 개정에 따른 법령명 수정\n\n"
                                + "2. 시행일자\n"
                                + "- 개정 약관 공지일: 2025년 1월 20일 (월)\n"
                                + "- 개정 약관 적용일: 2025년 1월 27일 (월)\n\n"
                                + "필루션은 고객님의 건강을 위해 항상 최선을 다하겠습니다.\n감사합니다.", "공지사항"),

                createPost(adminMember.get().getId(), noticeBoard, "사이트 점검 안내",
                        "필루션 웹사이트 정기 점검이 있을 예정입니다. (2025년 1월 31일 00:00 ~ 06:00)\n"
                                + "점검 시간 내에 웹사이트 서비스가 중단되오니\n고객 여러분의 양해를 부탁드립니다.\n감사합니다.", "공지사항"),

                createPost(adminMember.get().getId(), noticeBoard, "2025년 설 연휴 배송 및 휴무 일정 안내",
                        "안녕하세요.\n필루션입니다.\n\n"
                        + "긴 연휴로 영양제 배송이 늦어질 수 있어 안내드립니다."
                        + "■[배송 일정 공지]\n"
                        + "1월23일(목) 12시 결제 건까지 당일 출고되며, 이후 결제 건은 1월 31일(금)부터 순차 배송됩니다.\n\n"
                        + "[고객센터 휴무]\n"
                        + "필루션 고객센터는 1월 27일(월) ~ 1월 30일(목) 휴무입니다.\n"
                        + "문의 남겨 주시면 1월 31일(금) 오전 10시 부터 순차적으로 안내 도와 드리겠습니다.\n"
                        + "평온한 설 연휴 보내시길 기원합니다.\n\n"
                        + "건강하고 즐거운 설 연휴 보내세요 :)", "공지사항"),

                createPost(adminMember.get().getId(), noticeBoard, "필루션 회원 가입 시 추가 혜택 안내",
                        "안녕하세요, 필루션입니다.\n\n"
                        + "회원 가입을 통해 더 많은 혜택을 누려보세요! 필루션에서 제공하는 다양한 혜택을 회원 전용으로 누리실 수 있습니다.\n\n"
                        + "■ 회원 가입 시 제공되는 혜택:\n\n"
                        + "월간 적립금 1,000원 자동 적립\n"
                        + "신제품 출시 정보 및 이벤트 우선 안내\n"
                        + "회원 가입 기간: 2025년 2월 1일(토) ~ 2025년 2월 28일(금)\n\n"
                        + "회원 가입 후, 필루션의 다양한 서비스를 편리하게 이용하시기 바랍니다.\n"
                        + "감사합니다.\n\n"
                        + "필루션 드림\n", "공지사항"),

                // ✅ 자주 묻는 질문 11개
                createPost(adminMember.get().getId(), faqBoard, "건강설문 받은 제품만 구매할 수 있나요?",
                        "건강 설문 추천 제품 외에 제품 추가나 제거를 통해 변경하실 수 있어요.", "기타"),

                createPost(adminMember.get().getId(), faqBoard, "필루션 영양제와 다른 영양제를 함께 먹어도 될까요?",
                        "정확한 상담을 위해서는 드시고 계신 타사 영양제의 성분을 알기 위해 품명을 알아야 해요.\n"
                        + "품명과 함께 필루션 상담원에게 문의하시면 빠르고 정확하게 답변을 드릴게요.", "제품"),

                createPost(adminMember.get().getId(), faqBoard, "결제 취소는 어떻게 하나요?",
                        "주문 상태가 배송 전으로 표시되는 경우\n"
                        + "-> 결제일 당일 12시(정오) 이전\n"
                        + "홈페이지 -> 마이페이지 -> 결제관리에서 언제든 주문 취소가 가능해요.\n\n"
                        + "주문 상태가 배송 중 또는 배송 완료인 경우\n"
                        + "-> 결제일 당일 12시(정오) 이후\n"
                        + "필루션 상담원 채팅 상담을 통해 교환 및 반품 접수를 해주셔야 돼요.\n"
                        + "(위의 경우 및 배송 완료 후 취소의 경우에는 취소 수수료가 발생할 수 있어요)\n\n"
                        + "주문 취소가 된 건은 결제 수단에 따라 영업일 기준으로 3일~7일 이내에 승인 취소 또는 환불 처리를 해드려요.\n"
                        + "(결제 취소 시에는 해당 결제를 통해 제공된 혜택이 공제된 후에 제공돼요)", "교환/반품"),

                createPost(adminMember.get().getId(), faqBoard, "배송 조회는 어떻게 하나요?",
                        "정기구독으로 구매하신 경우\n"
                        + "홈페이지 -> 마이페이지 -> 결제관리 -> 해당 결제건 클릭 -> 운송장 번호 클릭\n\n"
                        + "비회원으로 한 번만 구매하기를 하신 경우\n"
                        + "홈페이지 -> 고객센터 -> 비회원 주문 -> 주문번호 또는 연락처 입력, 주문 확인 비밀번호 입력 -> 운송장 번호 클릭", "배송"),

                createPost(adminMember.get().getId(), faqBoard, "정기구독을 잠시 중단할 수 있나요?",
                        "정기구독 중단 기능은 없어요. 다만 최대 3개월까지 정기구독 결제일을 연기하실 수 있어요.\n"
                        + "홈페이지 -> 마이페이지 -> 정기구독에서 결제일 변경 또는 상담원 채팅을 통해 변경하실 수 있어요.\n"
                        + "※ 결제 2일 전에 결제 예정 알림을 드리니 안심하셔도 돼요.", "주문/결제"),

                createPost(adminMember.get().getId(), faqBoard, "정기구독 제품을 변경할 수 있나요?",
                        "정기구독 제품을 추가 또는 제거하실 수 있어요.\n\n"
                        + "제품 추가\n"
                        + "홈페이지 -> 마이페이지 -> 정기구독 -> 상품정보 -> 수정하기 -> 제품 추가하기 -> 상품정보 저장하기\n\n"
                        + "제품 제거\n"
                        + "홈페이지 -> 마이페이지 -> 정기구독 -> 상품정보 -> 수정하기 -> 제품 우측 x 표시 클릭 -> 상품정보 저장하기", "주문/결제"),

                createPost(adminMember.get().getId(), faqBoard, "비회원으로 정기구독을 할 수 없나요?",
                        "비회원으로는 정기구독을 하실 수 없으며 한 번만 구매하기를 통해 일회성으로 구매하실 수 있어요.", "주문/결제"),

                createPost(adminMember.get().getId(), faqBoard, "비밀번호가 생각나지 않아요.",
                        "필루션 홈페이지에서 회원가입하신 경우\n"
                        + "홈페이지 -> 로그인 -> 비밀번호 찾기\n\n"
                        + "SNS 계정으로 가입하신 경우(네이버, 카카오 등)\n"
                        + "가입하신 SNS를 통해 찾으셔야 돼요.\n"
                        + "상담원을 통해 가입 아이디만 알려드릴 수 있어요.", "회원정보"),

                createPost(adminMember.get().getId(), faqBoard, "회원정보 변경은 어떻게 하나요?",
                        "가입하신 회원정보 변경을 원하시는 경우\n"
                        + "홈페이지 -> 마이페이지 -> 회원관리에서 수정하실 수 있어요.\n\n"
                        + "계정에 등록된 연락처 변경 혹은 아이디 변경은\n"
                        + "상담원을 통해 요청하실 수 있어요.", "회원정보"),

                createPost(adminMember.get().getId(), faqBoard, "제품을 교환/반품하고 싶어요.",
                        "교환 및 반품의 경우 필루션 상담원을 통해 접수가 가능하며 아래의 교환 및 반품 규정에 따라 진행돼요.\n"
                        + "(필요 시 제품의 상태 이미지 첨부 필요)\n\n"
                        + "수령한 제품에 문제가 있는 경우\n"
                        + "제품이 표시 및 광고된 내용과 다르거나 제품 자체에 하자가 있는 경우에는 재배송 또는 환불 처리를 해드려요.\n"
                        + "제품을 받으시고 제품의 문제를 확인 가능한 사진을 첨부해서 필루션 상담원에게 전송 부탁드려요.\n"
                        + "제품에 문제가 있는 것으로 확인될 경우 모든 배송비는 필루션이 부담해요.\n\n"
                        + "고객님의 변심 또는 주문 오류가 있는 경우\n"
                        + "제품의 상태에 문제가 없는 경우에는 제품을 받으신 날부터 7일 이내에 교환 및 반품 신청이 가능해요.\n"
                        + "단순 변심에 의한 교환 및 반품을 요청하신 경우 제품 발송을 위한 배송비와 반품을 위한 배송비가 부담돼요.\n"
                        + "(필루션 정기구독의 경우 배송비 무료로 제공이 되나 본 경우에는 발송 배송비와 반품 배송비를 포함하여 5,000원을 부담하셔야 해요)\n"
                        + "제품에 따라 도서/산간지역의 경우 배송비가 추가될 수 있어요.\n\n"
                        + "교환 및 반품 불가 안내\n"
                        + "아래와 같은 사항에는 교환 및 반품이 어려울 수 있으니 양해 부탁드려요.\n"
                        + "- 고객님의 책임으로 제품이 훼손된 경우 (제품 박스의 스티커를 제거한 경우 포함)\n"
                        + "- 고객님의 사용으로 제품의 가치가 현저하게 감소한 경우", "교환/반품"),

                createPost(adminMember.get().getId(), faqBoard, "정기구독과 한 번만 구매하기의 차이가 무엇인가요?",
                        "정기구독이란?\n"
                        + "매월(30일 기준) 영양제가 소진되기 전에 정기적으로 배송되는 서비스에요.\n"
                        + "필루션은 고객님들의 건강을 지켜드리기 위해 꾸준히 영양성분을 섭취하는 것이 매우 중요하다고 생각하고 있어요.\n"
                        + "그래서 따로 신경 쓰지 않아도 정기적으로 배송이 되는 배송 서비스를 제공해 드려요.\n\n"
                        + "한 번만 구매하기란?\n"
                        + "일회성으로 제품을 낱개로 구매하실 수 있어요.\n"
                        + "(다만, 배송비가 추가돼요)", "주문/결제")

        );

        postRepository.saveAll(posts);
        System.out.println("✅ Post 데이터 초기화 완료");
    }

    private Post createPost(Long authorId, Board board, String title, String content, String category) {
        return Post.builder()
                .authorId(authorId)
                .board(board)
                .title(title)
                .content(content)
                .category(category)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
*/
