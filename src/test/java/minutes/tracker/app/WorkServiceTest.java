package minutes.tracker.app;

import minutes.tracker.app.dto.WorkDTO;
import minutes.tracker.app.model.Work;
import minutes.tracker.app.model.SearchResult;
import minutes.tracker.app.services.WorkService;
import minutes.tracker.config.root.RootContextConfig;
import minutes.tracker.config.root.TestConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

import static minutes.tracker.app.TestUtils.date;
import static minutes.tracker.app.TestUtils.time;
import static minutes.tracker.app.dto.WorkDTO.mapFromWorkEntity;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes={TestConfiguration.class, RootContextConfig.class})
public class WorkServiceTest {

    @Autowired
    private WorkService workService;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testFindWorksByDate() {
        SearchResult<Work> result = workService.findWorks(UserServiceTest.USERNAME, date(2015,1,1), date(2015,1,2), null ,null, 1);
        assertTrue("results not expected, total " + result.getResultsCount(), result.getResultsCount() == 4);
    }

    @Test
    public void testFindWorksByDateTime() {
        SearchResult<Work> result = workService.findWorks(UserServiceTest.USERNAME, date(2015,1,1), date(2015,1,2),
                time("11:00") ,time("14:00"), 1);
        assertTrue("results not expected, total " + result.getResultsCount(), result.getResultsCount() == 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromDateAfterToDate() {
        workService.findWorks(UserServiceTest.USERNAME, date(2015,1,2), date(2015,1,1), null ,null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromTimeAfterToTime() {
        workService.findWorks(UserServiceTest.USERNAME, date(2015,1,2), date(2015,1,1), time("12:00") ,time("11:00"), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromDateNull() {
        workService.findWorks(UserServiceTest.USERNAME, null, date(2015,1,1), time("12:00") ,time("11:00"), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toDateNull() {
        workService.findWorks(UserServiceTest.USERNAME, date(2015,1,1), null, time("12:00") ,time("11:00"), 1);
    }

    @Test
    public void deleteWorks() {
        workService.deleteWorks(Arrays.asList(15L));
        Work work = em.find(Work.class, 15L);
        assertNull("work was not deleted" , work);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteWorksNull() {
        workService.deleteWorks(null);
    }

    @Test
    public void saveWorks() {
        WorkDTO work1 = mapFromWorkEntity(em.find(Work.class, 1L));
        WorkDTO work2 = mapFromWorkEntity(em.find(Work.class, 2L));

        work1.setDescription("test1");
        work2.setMinutes(10L);

        List<WorkDTO> works = Arrays.asList(work1, work2);

        workService.saveWorks(UserServiceTest.USERNAME, works);


        Work m1 = em.find(Work.class, 1L);
        assertTrue("description not as expected: " + m1.getDescription(), "test1".equals(m1.getDescription()));

        Work m2 = em.find(Work.class, 2L);
        assertTrue("minutes not as expected: " + m2.getMinutes(), m2.getMinutes() == 10L);
    }


}
