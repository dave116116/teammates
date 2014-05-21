package teammates.test.cases.ui;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.FeedbackQuestionAttributes;
import teammates.common.datatransfer.FeedbackResponseAttributes;
import teammates.common.datatransfer.FeedbackSessionAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.util.Const;
import teammates.storage.api.FeedbackQuestionsDb;
import teammates.storage.api.FeedbackResponsesDb;
import teammates.ui.controller.ShowPageResult;
import teammates.ui.controller.StudentFeedbackQuestionSubmissionEditSaveAction;

public class StudentFeedbackQuestionSubmissionEditSaveActionTest extends
        BaseActionTest {

    DataBundle dataBundle;

    @BeforeClass
    public static void classSetUp() throws Exception {
        printTestClassHeader();
        uri = Const.ActionURIs.STUDENT_FEEDBACK_QUESTION_SUBMISSION_EDIT_SAVE;
    }

    @BeforeMethod
    public void caseSetUp() throws Exception {
        dataBundle = getTypicalDataBundle();
        restoreTypicalDataInDatastore();
    }

    @Test
    public void testAccessControl() throws Exception {

        FeedbackSessionAttributes session1InCourse1 = dataBundle.feedbackSessions
                .get("session1InCourse1");

        FeedbackQuestionsDb feedbackQuestionsDb = new FeedbackQuestionsDb();
        FeedbackQuestionAttributes feedbackQuestion = feedbackQuestionsDb
                .getFeedbackQuestion(session1InCourse1.feedbackSessionName,
                        session1InCourse1.courseId, 1);

        String[] submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.courseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                session1InCourse1.feedbackSessionName,
                Const.ParamsNames.FEEDBACK_QUESTION_ID,
                feedbackQuestion.getId(),
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL, "0"
        };

        verifyOnlyStudentsOfTheSameCourseCanAccess(submissionParams);
        verifyUnaccessibleForUnregisteredUsers(submissionParams);
        verifyUnaccessibleWithoutLogin(submissionParams);

    }

    @Test
    public void testExecuteAndPostProcess() throws Exception {

        StudentAttributes student1InCourse1 = dataBundle.students
                .get("student1InCourse1");
        FeedbackSessionAttributes session1InCourse1 = dataBundle.feedbackSessions
                .get("session1InCourse1");

        FeedbackQuestionsDb feedbackQuestionsDb = new FeedbackQuestionsDb();
        FeedbackQuestionAttributes feedbackQuestion = feedbackQuestionsDb
                .getFeedbackQuestion(
                        session1InCourse1.feedbackSessionName,
                        session1InCourse1.courseId, 1);

        FeedbackResponsesDb feedbackResponsesDb = new FeedbackResponsesDb();
        FeedbackResponseAttributes feedbackResponse = feedbackResponsesDb
                .getFeedbackResponse(feedbackQuestion.getId(),
                        student1InCourse1.email, student1InCourse1.email);

        assertNotNull(feedbackResponse);

        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        ______TS("not enough parameters");

        verifyAssumptionFailure();

        String[] submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.courseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                session1InCourse1.feedbackSessionName,
                Const.ParamsNames.FEEDBACK_QUESTION_ID,
                feedbackQuestion.getId()
        };
        verifyAssumptionFailure(submissionParams);

        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.courseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                session1InCourse1.feedbackSessionName,
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL, "0"
        };
        verifyAssumptionFailure(submissionParams);

        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID,
                feedbackQuestion.getId(),
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL, "0"
        };
        verifyAssumptionFailure(submissionParams);

        submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                session1InCourse1.feedbackSessionName,
                Const.ParamsNames.FEEDBACK_QUESTION_ID,
                feedbackQuestion.getId(),
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL, "0"
        };
        verifyAssumptionFailure(submissionParams);

        ______TS("edit existing answer");

        feedbackResponse = feedbackResponsesDb
                .getFeedbackResponse(feedbackQuestion.getId(),
                        student1InCourse1.email, student1InCourse1.email);

        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, feedbackResponse.courseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                session1InCourse1.feedbackSessionName,
                Const.ParamsNames.FEEDBACK_QUESTION_ID,
                feedbackQuestion.getId(),
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL, "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0",
                feedbackResponse.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE,
                feedbackQuestion.questionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "Qn Answer",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0",
                feedbackResponse.getId()
        };

        StudentFeedbackQuestionSubmissionEditSaveAction saveAction =
                getAction(submissionParams);
        ShowPageResult pageResult = getShowPageResult(saveAction);

        assertEquals(Const.ViewURIs.STUDENT_FEEDBACK_QUESTION_SUBMISSION_EDIT,
                pageResult.destination);
        assertFalse(pageResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED,
                pageResult.getStatusMessage());

        feedbackResponse = feedbackResponsesDb.getFeedbackResponse(
                feedbackQuestion.getId(), student1InCourse1.email,
                feedbackResponse.recipientEmail);
        assertEquals("Qn Answer", feedbackResponse.responseMetaData.getValue());

        ______TS("delete answer");

        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.courseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                session1InCourse1.feedbackSessionName,
                Const.ParamsNames.FEEDBACK_QUESTION_ID,
                feedbackQuestion.getId(),
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL, "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0",
                feedbackResponse.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE,
                feedbackQuestion.questionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0",
                feedbackResponse.getId()
        };

        saveAction = getAction(submissionParams);
        pageResult = getShowPageResult(saveAction);

        assertEquals(Const.ViewURIs.STUDENT_FEEDBACK_QUESTION_SUBMISSION_EDIT,
                pageResult.destination);
        assertFalse(pageResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED,
                pageResult.getStatusMessage());
        assertNull(feedbackResponsesDb.getFeedbackResponse(
                feedbackQuestion.getId(), student1InCourse1.email,
                feedbackResponse.recipientEmail));

        ______TS("skip question");

        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.courseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                session1InCourse1.feedbackSessionName,
                Const.ParamsNames.FEEDBACK_QUESTION_ID,
                feedbackQuestion.getId(),
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL, "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0",
                feedbackResponse.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE,
                feedbackQuestion.questionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", ""
        };

        saveAction = getAction(submissionParams);
        pageResult = getShowPageResult(saveAction);

        assertEquals(Const.ViewURIs.STUDENT_FEEDBACK_QUESTION_SUBMISSION_EDIT,
                pageResult.destination);
        assertFalse(pageResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED,
                pageResult.getStatusMessage());
        assertNull(feedbackResponsesDb.getFeedbackResponse(
                feedbackQuestion.getId(), student1InCourse1.email,
                feedbackResponse.recipientEmail));

        ______TS("new response");

        submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session1InCourse1.courseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                session1InCourse1.feedbackSessionName,
                Const.ParamsNames.FEEDBACK_QUESTION_ID,
                feedbackQuestion.getId(),
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL, "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0",
                feedbackResponse.recipientEmail,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE,
                feedbackQuestion.questionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0",
                "new response"
        };

        saveAction = getAction(submissionParams);
        pageResult = getShowPageResult(saveAction);

        assertEquals(Const.ViewURIs.STUDENT_FEEDBACK_QUESTION_SUBMISSION_EDIT,
                pageResult.destination);
        assertFalse(pageResult.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED,
                pageResult.getStatusMessage());

        /*
         * Attention Here: only in the last test case feedbackResponse object is
         * updated from the Db. This is because we need the info inside it, so
         * need to keep it not null Here is the last case, we can extracted the
         * latest response and assign the value to this object It is safe
         * because the response we get from the Db is not null
         * 
         * If additional test cases need to be added in this method, do as the
         * above cases -- directly get the data from Db and compare, do not
         * assign its value to this object and then compare
         */
        feedbackResponse = feedbackResponsesDb.getFeedbackResponse(
                feedbackQuestion.getId(), student1InCourse1.email,
                feedbackResponse.recipientEmail);
        assertEquals("new response", feedbackResponse.getResponseDetails()
                .getAnswerString());

    }

    private StudentFeedbackQuestionSubmissionEditSaveAction getAction(
            String... params) throws Exception {

        return (StudentFeedbackQuestionSubmissionEditSaveAction) (gaeSimulation
                .getActionObject(uri, params));

    }
}
