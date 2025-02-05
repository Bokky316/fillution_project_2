import React, { useEffect, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Box, Typography, Button, CircularProgress } from "@mui/material";
import {
  fetchCategories,
  fetchQuestions,
  updateResponse,
  setCurrentCategoryIndex,
  setCurrentSubCategoryIndex,
  submitSurvey,
  clearResponses,
  setFilteredSubCategories,
  setFilteredCategories
} from '@/redux/surveySlice';
import QuestionComponent from '@features/survey/QuestionComponent';

const SurveyPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  // Redux 상태에서 필요한 데이터 추출
  const {
    categories,
    currentCategoryIndex,
    currentSubCategoryIndex,
    questions,
    responses,
    categoriesLoading,
    questionsLoading,
    categoriesError,
    questionsError,
    gender,
    filteredSubCategories,
    selectedSymptoms,
    filteredCategories
  } = useSelector(state => state.survey);

  // 컴포넌트 마운트 시 카테고리 데이터 로드
  useEffect(() => {
    dispatch(fetchCategories());
  }, [dispatch]);

  // 현재 카테고리와 서브카테고리에 해당하는 질문 로드
  useEffect(() => {
    const categoriesToUse = filteredCategories || categories;
    if (categoriesToUse.length > 0 && currentCategoryIndex !== null && currentSubCategoryIndex !== null) {
      const subCategoriesToUse = filteredSubCategories || categoriesToUse[currentCategoryIndex]?.subCategories;
      const subCategoryId = subCategoriesToUse?.[currentSubCategoryIndex]?.id;
      if (subCategoryId) {
        dispatch(fetchQuestions(subCategoryId));
      }
    }
  }, [dispatch, categories, filteredCategories, currentCategoryIndex, currentSubCategoryIndex, filteredSubCategories]);

  // 컴포넌트 언마운트 시 응답 초기화
  useEffect(() => {
    return () => {
      dispatch(clearResponses());
    };
  }, [dispatch]);

  // 성별 및 증상에 따른 카테고리 및 서브카테고리 필터링
  useEffect(() => {
    const categoriesToUse = filteredCategories || categories;
    const currentCategory = categoriesToUse[currentCategoryIndex];

    // 성별에 따른 카테고리 필터링
    if (gender) {
      console.log("Current Gender:", gender); // 디버깅용

      const filteredCats = categoriesToUse.map(category => {
        if (category.name === "3. 생활 습관") {
          const filteredSubCategories = category.subCategories.filter(sub => {
            if (gender === '여성' && sub.name === "여성건강") return true;
            if (gender === '남성' && sub.name === "남성건강") return true;
            return sub.name !== "여성건강" && sub.name !== "남성건강";
          });

          console.log("Filtered Subcategories for 생활 습관:", filteredSubCategories); // 디버깅용

          return { ...category, subCategories: filteredSubCategories };
        }
        return category;
      });

      console.log("Filtered Categories:", filteredCats); // 디버깅용

      dispatch(setFilteredCategories(filteredCats));
    } else {
      dispatch(setFilteredCategories(null));
    }

    // 증상에 따른 서브카테고리 필터링
    if (currentCategory?.name === "2. 증상·불편" && selectedSymptoms.length > 0) {
      console.log("Selected Symptoms:", selectedSymptoms); // 디버깅용

      const symptomQuestion = currentCategory.subCategories
        .find(sub => sub.name === "주요 증상")
        ?.questions[0];

      if (symptomQuestion) {
        const filteredSubs = currentCategory.subCategories.filter(sub => {
          if (sub.name === "주요 증상" || sub.name === "추가 증상") {
            return true;
          }
          return selectedSymptoms.some(symptomId => {
            const symptomOption = symptomQuestion.options
              .find(opt => opt.id === parseInt(symptomId, 10));
            return symptomOption && sub.name.toLowerCase().includes(symptomOption.optionText.toLowerCase());
          });
        });

        console.log("Filtered Subcategories:", filteredSubs); // 디버깅용

        if (filteredSubs.length > 0) {
          dispatch(setFilteredSubCategories(filteredSubs));
        } else {
          dispatch(setFilteredSubCategories(null));
        }
      }
    } else {
      dispatch(setFilteredSubCategories(null));
    }
  }, [categories, currentCategoryIndex, selectedSymptoms, gender, dispatch]);

  // 응답 변경 핸들러
  const handleResponseChange = useCallback((questionId, value) => {
    dispatch(updateResponse({ questionId, value }));
  }, [dispatch]);

  // 이전 버튼 핸들러
  const handlePrevious = useCallback(() => {
    const categoriesToUse = filteredCategories || categories;
    if (currentSubCategoryIndex > 0) {
      dispatch(setCurrentSubCategoryIndex(currentSubCategoryIndex - 1));
    } else if (currentCategoryIndex > 0) {
      dispatch(setCurrentCategoryIndex(currentCategoryIndex - 1));
      const prevCategory = categoriesToUse[currentCategoryIndex - 1];
      dispatch(setCurrentSubCategoryIndex(prevCategory.subCategories.length - 1));
    }
  }, [dispatch, filteredCategories, categories, currentSubCategoryIndex, currentCategoryIndex]);

  // 다음 버튼 핸들러
  const handleNext = useCallback(() => {
    const categoriesToUse = filteredCategories || categories;
    const currentCategory = categoriesToUse[currentCategoryIndex];
    const subCategoriesToUse = filteredSubCategories || currentCategory?.subCategories;

    if (currentCategoryIndex === categoriesToUse.length - 1 &&
        currentSubCategoryIndex === subCategoriesToUse.length - 1) {
      // 마지막 질문이면 응답 제출
      const formattedResponses = Object.entries(responses).map(([questionId, answer]) => ({
        questionId: Number(questionId),
        responseType: questions.find(q => q.id === Number(questionId))?.questionType,
        responseText: typeof answer === 'string' ? answer : null,
        selectedOptions: Array.isArray(answer) ? answer.map(Number) : (typeof answer === 'number' ? [answer] : null)
      })).filter(response => response.responseText !== null || (response.selectedOptions && response.selectedOptions.length > 0));

      if (formattedResponses.length === 0) {
        alert('제출할 응답이 없습니다.');
        return;
      }

      dispatch(submitSurvey({ responses: formattedResponses }))
        .unwrap()
        .then(() => {
          dispatch(clearResponses());
          navigate('/survey-complete');
        })
        .catch(error => {
          alert(`제출 오류: ${error}`);
        });
    } else {
      // 다음 질문으로 이동
      if (currentSubCategoryIndex < subCategoriesToUse.length - 1) {
        dispatch(setCurrentSubCategoryIndex(currentSubCategoryIndex + 1));
      } else if (currentCategoryIndex < categoriesToUse.length - 1) {
        dispatch(setCurrentCategoryIndex(currentCategoryIndex + 1));
        dispatch(setCurrentSubCategoryIndex(0));
      }
    }
  }, [dispatch, filteredCategories, categories, currentCategoryIndex, currentSubCategoryIndex, filteredSubCategories, responses, questions, navigate]);

  // 다음 버튼 비활성화 여부 확인
  const isNextButtonDisabled = useCallback(() => {
    return questions.some(question => !responses[question.id]);
  }, [questions, responses]);

  // 로딩 중 표시
  if (categoriesLoading || questionsLoading) {
    return <CircularProgress />;
  }

  // 에러 표시
  if (categoriesError || questionsError) {
    return <Typography color="error">{categoriesError || questionsError}</Typography>;
  }

  // 카테고리가 없는 경우
  if (!categories || categories.length === 0) {
    return <Typography>카테고리가 없습니다. 관리자에게 문의하세요.</Typography>;
  }

  // 현재 카테고리 또는 서브카테고리 인덱스가 없는 경우
  if (currentCategoryIndex === null || currentSubCategoryIndex === null) {
    return <Typography>설문을 불러오는 중입니다...</Typography>;
  }

  const categoriesToUse = filteredCategories || categories;
  const currentCategory = categoriesToUse[currentCategoryIndex];
  const subCategoriesToDisplay = filteredSubCategories || currentCategory?.subCategories;
  const currentSubCategory = subCategoriesToDisplay?.[currentSubCategoryIndex];

  // 메인 렌더링
  return (
    <Box sx={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      {currentCategory && currentSubCategory && (
        <>
          <Typography variant="h5" sx={{ mb: 3 }}>{currentCategory.name}</Typography>
          <Typography variant="h6" sx={{ mb: 2 }}>{currentSubCategory.name}</Typography>
          {questions.map((question) => (
            <QuestionComponent
              key={question.id}
              question={question}
              response={responses[question.id]}
              onResponseChange={(value) => handleResponseChange(question.id, value)}
            />
          ))}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
            <Button
              variant="contained"
              onClick={handlePrevious}
              disabled={currentCategoryIndex === 0 && currentSubCategoryIndex === 0}
            >
              이전
            </Button>
            <Button
              variant="contained"
              onClick={handleNext}
              disabled={isNextButtonDisabled()}
            >
              {currentCategoryIndex === categoriesToUse.length - 1 &&
               currentSubCategoryIndex === subCategoriesToDisplay.length - 1
               ? '제출' : '다음'}
            </Button>
          </Box>
        </>
      )}
    </Box>
  );
};

export default SurveyPage;
