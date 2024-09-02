package com.tranquility.core;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
//@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ErpDataHandlerTests {

    @Autowired
    private MockMvc mockMvc;

    private String json;

    @BeforeEach
    public void createRequestBody() {
        String username = "2023BAIML041";
        String password = "I@mPeace200";
        json = "{\"loginDetails\":{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" } }";
    }

//    @Test
    public void createSession() throws Exception {
       getSession();
    }

    public MockHttpSession getSession() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(MockMvcRequestBuilders.post("/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(content().string("Session Created"))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();

        return session;
    }

//    @Test
    public void login() throws Exception {
        MockHttpSession session = getSession();
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/login-me")
                        .session(session))
                .andExpect(status().isOk()).andReturn().getResponse();

        System.out.println(response.getContentAsString());
    }

//    @Test
    public void getData() throws Exception {
        MockHttpSession session = getSession();
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/student-data")
                .session(session))
                .andExpect(status().isOk()).andReturn().getResponse();

        System.out.println("Response: " + response.getContentAsString());
    }

//    @Test
    public void getAttendance() throws Exception {
        MockHttpSession session = getSession();
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/attendance")
                .session(session))
                .andExpect(status().isOk()).andReturn().getResponse();

        System.out.println("Response: " + response.getContentAsString());
    }

//    @Test
    public void getSubjects() throws Exception {
        MockHttpSession session = getSession();
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/subjects")
                .session(session))
                .andExpect(status().isOk()).andReturn().getResponse();

        System.out.println("Response: " + response.getContentAsString());
    }

//    @Test
    public void getClassmates() throws Exception {
        MockHttpSession session = getSession();
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/classmates")
                .session(session))
                .andExpect(status().isOk()).andReturn().getResponse();

        System.out.println("Response: " + response.getContentAsString());
    }

//    @Test
    public void getCirculars() throws Exception {
        MockHttpSession session = getSession();
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/circulars")
                .session(session))
                .andExpect(status().isOk()).andReturn().getResponse();

        System.out.println("Response: " + response.getContentAsString());
    }

//    @Test
    public void getAcademicCalendar() throws Exception {
        MockHttpSession session = getSession();
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/academic-calendar")
                .session(session))
                .andExpect(status().isOk()).andReturn().getResponse();

        System.out.println("Response: " + response.getContentAsString());
    }

}
