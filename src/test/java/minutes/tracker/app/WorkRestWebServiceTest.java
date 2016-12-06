package minutes.tracker.app;

import minutes.tracker.app.dao.WorkRepository;
import minutes.tracker.app.model.Work;
import minutes.tracker.config.root.RootContextConfig;
import minutes.tracker.config.root.TestConfiguration;
import minutes.tracker.config.servlet.ServletContextConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sun.security.acl.PrincipalImpl;

import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles("test")
@ContextConfiguration(classes={TestConfiguration.class, RootContextConfig.class, ServletContextConfig.class})
public class WorkRestWebServiceTest {

    private MockMvc mockMvc;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void init()  {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testSearchWorksByDate() throws Exception {
        mockMvc.perform(get("/work")
                .param("fromDate", "2015/01/01")
                .param("toDate", "2015/01/02")
                .param("pageNumber", "1")
                .accept(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl("test123")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.['works'].[0].['description']").value("2 -  Chickpea with roasted cauliflower"));
    }

    @Test
    public void testSaveWorks() throws Exception {
        mockMvc.perform(post("/work")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"id\":\"1\", \"date\": \"2015/01/01\",\"time\": \"11:00\", \"minutes\":\"100\", \"description\": \"test\" }]")
                .accept(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(UserServiceTest.USERNAME)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.[0].['description']").value("test"));
    }

    @Test
    public void deleteWorks() throws Exception {
        mockMvc.perform(delete("/work")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[14]")
                .accept(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(UserServiceTest.USERNAME)))
                .andDo(print())
                .andExpect(status().isOk());

        Work work = workRepository.findWorkById(14L);
        assertNull("work no deleted", work);
    }

}
