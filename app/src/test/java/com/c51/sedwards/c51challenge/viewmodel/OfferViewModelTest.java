package com.c51.sedwards.c51challenge.viewmodel;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.content.res.AssetManager;

import com.c51.sedwards.c51challenge.model.OfferList;
import com.google.gson.Gson;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OfferViewModelTest {
    public static final String DEFAULT_FILE_NAME = "c51.json";

    //For testing LiveData
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock
    Application mockApp;

    @Mock
    AssetManager mockManager;

    @Mock
    Observer<OfferList> mockObserver;

    InputStream mockInputStream = new ByteArrayInputStream(TestVals.JSON_STRING.getBytes());

    private void setup() throws IOException {
        when(mockManager.open(DEFAULT_FILE_NAME)).thenReturn(mockInputStream);
        when(mockApp.getAssets()).thenReturn(mockManager);
        when(mockApp.getCacheDir()).thenReturn(null);
    }

    /**
     * Test update observable called
     * @throws IOException
     */
    @Test
    public void testFetchOffersChangedIsCalled() throws IOException {
        setup();
        OfferViewModel viewModel;

        OfferList list = new Gson().fromJson(new InputStreamReader(mockInputStream), OfferList.class);

        viewModel = new OfferViewModel();
        viewModel.setApplication(mockApp);

        viewModel.getOffers().observeForever(mockObserver);

        Mockito.verify(mockObserver).onChanged(any(OfferList.class));
    }

    /**
     * Test contents are as expected
     * @throws IOException
     */
    @Test
    public void testFetchOffersHasC51Response() throws IOException {
        setup();
        OfferViewModel viewModel;

        OfferList list = new Gson().fromJson(new InputStreamReader(mockInputStream), OfferList.class);

        viewModel = new OfferViewModel();
        viewModel.setApplication(mockApp);

        viewModel.getOffers().observeForever(mockObserver);

        ArgumentCaptor<OfferList> argumentCaptor = ArgumentCaptor.forClass(OfferList.class);
        Mockito.verify(mockObserver).onChanged(argumentCaptor.capture());
        OfferList resultList = argumentCaptor.getValue();
        assertNotNull(resultList);
        assertNotNull(resultList.getOffers());
        assertNotNull(resultList.getOffers().get(0));
        assertEquals(resultList.getOffers().get(0).getOfferId(), list.getOffers().get(0).getOfferId());
    }
}