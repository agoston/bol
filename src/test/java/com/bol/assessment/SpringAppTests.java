package com.bol.assessment;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {GameMain.class})
@WebAppConfiguration
@IntegrationTest
public class SpringAppTests {
    @Test
    public void testSayHello() {
        RestTemplate restTemplate = new RestTemplate();
        GameState result = restTemplate.getForObject("http://localhost:8080/greeting", GameState.class);
        assertThat(result.getId(), is(1L));
        assertThat(result.getContent(), is("Hello, World!"));
    }
}
