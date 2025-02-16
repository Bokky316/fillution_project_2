/*


package com.javalab.student.config.DataInitializer;


import com.javalab.student.entity.*;
import com.javalab.student.entity.healthSurvey.QuestionOption;
import com.javalab.student.entity.healthSurvey.SurveyCategory;
import com.javalab.student.entity.healthSurvey.SurveyQuestion;
import com.javalab.student.entity.healthSurvey.SurveySubCategory;
import com.javalab.student.repository.*;
import com.javalab.student.repository.healthSurvey.QuestionOptionRepository;
import com.javalab.student.repository.healthSurvey.SurveyCategoryRepository;
import com.javalab.student.repository.healthSurvey.SurveyQuestionRepository;
import com.javalab.student.repository.healthSurvey.SurveySubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SurveyCategoryRepository categoryRepository;
    private final SurveySubCategoryRepository subCategoryRepository;
    private final SurveyQuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 1. 카테고리 생성
        SurveyCategory category1 = createCategory("기본 정보");
        SurveyCategory category2 = createCategory("증상·불편");
        SurveyCategory category3 = createCategory("생활 습관");

        // 카테고리별 서브카테고리 및 질문 생성
        createBasicInfoQuestions(category1);
        createSymptomsQuestions(category2);
        createLifestyleQuestions(category3);
    }

    private SurveyCategory createCategory(String name) {
        List<SurveyCategory> existingCategories = categoryRepository.findAll();
        for (SurveyCategory category : existingCategories) {
            if (category.getName().equals(name)) {
                return category;
            }
        }
        SurveyCategory category = new SurveyCategory();
        category.setName(name);
        return categoryRepository.save(category);
    }

    private SurveySubCategory createSubCategory(String name, SurveyCategory category) {
        List<SurveySubCategory> existingSubCategories = subCategoryRepository.findAll();
        for (SurveySubCategory subCategory : existingSubCategories) {
            if (subCategory.getName().equals(name) && subCategory.getCategory().equals(category)) {
                return subCategory;
            }
        }
        SurveySubCategory subCategory = new SurveySubCategory();
        subCategory.setName(name);
        subCategory.setCategory(category);
        return subCategoryRepository.save(subCategory);
    }

    private SurveyQuestion createQuestion(String text, String type, int order, SurveySubCategory subCategory) {
        List<SurveyQuestion> existingQuestions = questionRepository.findAll();
        for (SurveyQuestion question : existingQuestions) {
            if (question.getQuestionText().equals(text) && question.getSubCategory().equals(subCategory)) {
                return question;
            }
        }
        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionText(text);
        question.setQuestionType(type);
        question.setQuestionOrder(order);
        question.setSubCategory(subCategory);
        question.setCategory(subCategory.getCategory());
        return questionRepository.save(question);
    }

    private void createOptions(SurveyQuestion question, List<String> optionTexts) {
        List<QuestionOption> existingOptions = optionRepository.findAll();
        for (String optionText : optionTexts) {
            boolean exists = false;
            for (QuestionOption existingOption : existingOptions) {
                if (existingOption.getOptionText().equals(optionText) && existingOption.getQuestion().equals(question)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                QuestionOption option = new QuestionOption();
                option.setOptionText(optionText);
                option.setOptionOrder(existingOptions.size() + 1);
                option.setQuestion(question);
                optionRepository.save(option);
            }
        }
    }


    private void createBasicInfoQuestions(SurveyCategory basicInfo) {
    // 이름
    SurveySubCategory nameSubCategory = createSubCategory("이름", basicInfo);
    createQuestion("이름을 알려주세요", "TEXT", 1, nameSubCategory);

    // 성별
    SurveySubCategory genderSubCategory = createSubCategory("성별", basicInfo);
    SurveyQuestion genderQ = createQuestion("성별을 알려주세요", "SINGLE_CHOICE", 1, genderSubCategory);
    createOptions(genderQ, Arrays.asList("여성", "남성"));

    // 나이
    SurveySubCategory ageSubCategory = createSubCategory("나이", basicInfo);
    createQuestion("나이를 알려주세요", "TEXT", 1, ageSubCategory);

    // 키
    SurveySubCategory heightSubCategory = createSubCategory("키", basicInfo);
    createQuestion("키를 알려주세요", "TEXT", 1, heightSubCategory);

    // 몸무게
    SurveySubCategory weightSubCategory = createSubCategory("몸무게", basicInfo);
    createQuestion("몸무게를 알려주세요", "TEXT", 1, weightSubCategory);
}


    private void createSymptomsQuestions(SurveyCategory symptoms) {
        // 주요 증상
        SurveySubCategory mainSymptoms = createSubCategory("주요 증상", symptoms);
        SurveyQuestion mainSymptomsQ = createQuestion(
                "불편하거나 걱정되는 것을 최대 3가지 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                mainSymptoms
        );
        createOptions(mainSymptomsQ, Arrays.asList(
                "혈관·혈액순환",
                "소화·장",
                "피부",
                "눈",
                "두뇌 활동",
                "피로감",
                "뼈·관절",
                "면역",
                "모발",
                "선택할 것이 없어요"
        ));

        createBloodCirculationQuestions(symptoms);
        createDigestionQuestions(symptoms);
        createSkinQuestions(symptoms);
        createEyeQuestions(symptoms);
        createBrainActivityQuestions(symptoms);
        createFatigueQuestions(symptoms);
        createBoneJointQuestions(symptoms);
        createImmuneQuestions(symptoms);
        createHairQuestions(symptoms);
        createAdditionalSymptomsQuestions(symptoms);
    }

    private void createBloodCirculationQuestions(SurveyCategory symptoms) {
        SurveySubCategory bloodCirculation = createSubCategory("혈관·혈액순환", symptoms);
        SurveyQuestion q = createQuestion(
                "혈관 건강에 해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                bloodCirculation
        );
        createOptions(q, Arrays.asList(
                "상처가 잘 낫지 않아요",
                "손발 끝이 자주 저려요",
                "잇몸이 붓고 피가 나요",
                "얼굴이 자주 창백해져요",
                "선택할 것은 없지만 혈관·혈액순환이 걱정돼요"
        ));
    }

    private void createDigestionQuestions(SurveyCategory symptoms) {
        SurveySubCategory digestion = createSubCategory("소화·장", symptoms);
        SurveyQuestion q = createQuestion(
                "소화·장 건강에 해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                digestion
        );
        createOptions(q, Arrays.asList(
                "복통이나 속 쓰림이 자주 발생해요",
                "변비가 있어요",
                "변이 묽은 편이에요",
                "술을 마시면 얼굴이나 몸이 붉어지고 소화가 안 돼요",
                "잔뇨감이 있어요",
                "선택할 것은 없지만 소화력 개선이 필요해요"
        ));
    }

    private void createSkinQuestions(SurveyCategory symptoms) {
        SurveySubCategory skin = createSubCategory("피부", symptoms);
        SurveyQuestion q = createQuestion(
                "피부 건강에 해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                skin
        );
        createOptions(q, Arrays.asList(
                "피부가 건조하고 머리에 비듬이 많이 생겨요",
                "여드름이 많아서 걱정이에요",
                "피부에 염증이 자주 생겨요",
                "입안이 헐고 입술이 자주 갈라져요",
                "선택할 것은 없지만 피부건강이 걱정돼요"
        ));
    }

    private void createEyeQuestions(SurveyCategory symptoms) {
        SurveySubCategory eye = createSubCategory("눈", symptoms);
        SurveyQuestion q = createQuestion(
                "눈 건강에 해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                eye
        );
        createOptions(q, Arrays.asList(
                "눈이 건조해 뻑뻑하고 가려워요",
                "눈 주변이 떨려요",
                "핸드폰, 모니터를 본 후 시야가 흐릿해요",
                "어두워지면 시력이 저하돼요",
                "선택할 것은 없지만 눈 건강이 걱정돼요"
        ));
    }

    private void createBrainActivityQuestions(SurveyCategory symptoms) {
        SurveySubCategory brain = createSubCategory("두뇌 활동", symptoms);
        SurveyQuestion q = createQuestion(
                "두뇌 건강에 해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                brain
        );
        createOptions(q, Arrays.asList(
                "기억력이 떨어지는 것 같아요",
                "두통이 자주 생겨요",
                "불안이나 긴장을 자주 느껴요",
                "우울한 감정을 자주 느껴요",
                "귀에서 울리는 소리가 가끔 나요",
                "선택할 것은 없지만 두뇌 활동이 걱정돼요"
        ));
    }

    private void createFatigueQuestions(SurveyCategory symptoms) {
        SurveySubCategory fatigue = createSubCategory("피로감", symptoms);
        SurveyQuestion q = createQuestion(
                "피로감에 해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                fatigue
        );
        createOptions(q, Arrays.asList(
                "무기력하고 식욕이 없어요",
                "자고 일어나도 피곤해요",
                "신경이 예민하고 잠을 잘 이루지 못해요",
                "잠을 잘 자요",
                "소변을 보기 위해 잠을 깨요",
                "선택할 것은 없지만 피로감이 있어요"
        ));
    }

    private void createBoneJointQuestions(SurveyCategory symptoms) {
        SurveySubCategory boneJoint = createSubCategory("뼈·관절", symptoms);
        SurveyQuestion q = createQuestion(
                "뼈·관절 건강에 해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                boneJoint
        );
        createOptions(q, Arrays.asList(
                "뼈가 부러진 경험이 있어요",
                "뼈가 약하다고 느껴요",
                "최근 1년 중 스테로이드를 섭취한 기간이 3개월 이상이에요",
                "선택할 것은 없지만 뼈·관절이 걱정돼요"
        ));
    }

    private void createImmuneQuestions(SurveyCategory symptoms) {
        SurveySubCategory immune = createSubCategory("면역", symptoms);
        SurveyQuestion q = createQuestion(
                "면역 건강에 해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                immune
        );
        createOptions(q, Arrays.asList(
                "스트레스가 매우 많아요",
                "감염성 질환에 자주 걸려요",
                "선택할 것은 없지만 면역이 걱정돼요"
        ));
    }

    private void createHairQuestions(SurveyCategory symptoms) {
        SurveySubCategory hair = createSubCategory("모발", symptoms);
        SurveyQuestion q = createQuestion(
                "모발 건강에 해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                hair
        );
        createOptions(q, Arrays.asList(
                "머리카락에 힘이 없고 잘 빠져요",
                "머리카락이 윤기 없고 갈라지고 끊어져요",
                "새치가 많이 나요",
                "선택할 것은 없지만 모발 건강이 걱정돼요"
        ));
    }

    private void createAdditionalSymptomsQuestions(SurveyCategory symptoms) {
        SurveySubCategory additionalSymptoms = createSubCategory("추가 증상", symptoms);
        SurveyQuestion q = createQuestion(
                "해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                additionalSymptoms
        );
        createOptions(q, Arrays.asList(
                "혈압이 높아요",
                "혈압이 낮아요",
                "더위를 타고 땀을 많이 흘려요",
                "항응고제를 복용 중이에요",
                "알레르기가 있어요",
                "해당사항 없음"
        ));
    }

    private void createLifestyleQuestions(SurveyCategory lifestyle) {
        createExerciseQuestions(lifestyle);
        createDietQuestions(lifestyle);
        createPreferencesQuestions(lifestyle);
        createPatternsQuestions(lifestyle);
        createFamilyHistoryQuestions(lifestyle);
        createFemaleHealthQuestions(lifestyle);
        createMaleHealthQuestions(lifestyle);
    }

private void createExerciseQuestions(SurveyCategory lifestyle) {
    // 운동 빈도
    SurveySubCategory exerciseFrequency = createSubCategory("운동 빈도", lifestyle);
    SurveyQuestion exerciseQ1 = createQuestion(
            "운동은 얼마나 자주 하시나요?",
            "SINGLE_CHOICE",
            1,
            exerciseFrequency
    );
    createOptions(exerciseQ1, Arrays.asList(
            "주 4회 이상",
            "주 2~3회",
            "주 1회 이하"
    ));

    // 야외활동 시간
    SurveySubCategory outdoorActivity = createSubCategory("야외활동 시간", lifestyle);
    SurveyQuestion exerciseQ2 = createQuestion(
            "햇빛을 쬐는 야외활동을 하루에 얼마나 하나요?",
            "SINGLE_CHOICE",
            1,
            outdoorActivity
    );
    createOptions(exerciseQ2, Arrays.asList(
            "4시간 이상",
            "1~4시간",
            "1시간 이하"
    ));
}


    private void createDietQuestions(SurveyCategory lifestyle) {
        SurveySubCategory diet = createSubCategory("식습관", lifestyle);
        SurveyQuestion q = createQuestion(
                "해당하는 식습관을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                diet
        );
        createOptions(q, Arrays.asList(
                "생선을 자주 먹어요",
                "채소를 자주 먹어요",
                "과일을 자주 먹어요",
                "고기를 자주 먹어요",
                "단 음식을 자주 먹어요",
                "식사를 자주 걸러요",
                "선택할 것이 없어요"
        ));
    }

    private void createPreferencesQuestions(SurveyCategory lifestyle) {
        SurveySubCategory preferences = createSubCategory("기호식품", lifestyle);
        SurveyQuestion q = createQuestion(
                "해당하는 기호식품 섭취 습관을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                preferences
        );
        createOptions(q, Arrays.asList(
                "담배를 피워요",
                "커피를 마셔요",
                "물을 잘 안 마셔요",
                "인스턴트 음식을 자주 먹어요",
                "선택할 것이 없어요"
        ));
    }

    private void createPatternsQuestions(SurveyCategory lifestyle) {
        SurveySubCategory patterns = createSubCategory("생활 패턴", lifestyle);
        SurveyQuestion q = createQuestion(
                "해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                patterns
        );
        createOptions(q, Arrays.asList(
                "업무, 학업 강도가 높아요",
                "핸드폰, 모니터를 오래 봐요",
                "목이 자주 건조하거나 칼칼해요",
                "집중력이 필요한 시기예요",
                "식사량을 줄이는 다이어트 중이에요",
                "구내염이 자주 생겨요",
                "선택할 것이 없어요"
        ));
    }

    private void createFamilyHistoryQuestions(SurveyCategory lifestyle) {
        SurveySubCategory family = createSubCategory("가족력", lifestyle);
        SurveyQuestion q = createQuestion(
                "가족력을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                family
        );
        createOptions(q, Arrays.asList(
                "간 질환이 있어요",
                "혈관 질환이 있어요",
                "뼈·관절 질환이 있어요",
                "당뇨가 있어요",
                "가족력이 없어요"
        ));
    }

    private void createFemaleHealthQuestions(SurveyCategory lifestyle) {
        SurveySubCategory femaleHealth = createSubCategory("여성건강", lifestyle);
        SurveyQuestion q = createQuestion(
                "해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                femaleHealth
        );
        createOptions(q, Arrays.asList(
                "임신, 수유 중이에요",
                "생리전 증후군, 유방 통증이 있어요",
                "요로감염, 잔뇨감과 같은 비뇨기계 질환이 있거나 걱정돼요",
                "생리 전후로 우울하거나 예민해요",
                "부정 출혈이 월 1회 이상 나타나요",
                "선택할 것이 없어요"
        ));
    }

    private void createMaleHealthQuestions(SurveyCategory lifestyle) {
        SurveySubCategory maleHealth = createSubCategory("남성건강", lifestyle);
        SurveyQuestion q = createQuestion(
                "해당하는 것을 모두 선택하세요",
                "MULTIPLE_CHOICE",
                1,
                maleHealth
        );
        createOptions(q, Arrays.asList(
                "남성 가족 중 비뇨기계 질환이 있어요",
                "이유 불문 머리가 빠지고 머리숱이 적어졌어요",
                "남성 불임에 대한 불안감이 있거나 2세 계획이 지연되고 있어요",
                "선택할 것이 없어요"
        ));
    }
}


*/
