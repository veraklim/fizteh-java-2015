package ru.fizteh.fivt.students.veraklim.TwitterStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import twitter4j.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class TwitterStreamTest {
    @Mock
    private Twitter twitter;

    @InjectMocks
    private TwitterStream search;
    public static List<Status> statuses;

    @Before
    public void setUp() throws Exception {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getTweets()).thenReturn(statuses);
        when(queryResult.nextQuery()).thenReturn(null);
        QueryResult emptyQueryResult = mock(QueryResult.class);
        when(emptyQueryResult.getTweets()).thenReturn(new ArrayList());
        when(queryResult.nextQuery()).thenReturn(null);
    }

    @Test
    public void simpleSetQueryTest() throws Exception {
        Parameters param = new Parameters();
        param.setQuery("cook");
        param.setStream(true);
        param.setLimit(40);
        param.setHideRetweets(true);
        param.setHelp(false);
        param.setPlace("Moscow");
        Query query = search.setQuery(param);
        assertEquals(query.getQuery(), "cook");
    }
}
