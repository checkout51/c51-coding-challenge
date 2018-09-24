package com.c51.sedwards.c51challenge.adapter;

import android.support.v7.widget.RecyclerView;

import com.c51.sedwards.c51challenge.OfferListActivity;
import com.c51.sedwards.c51challenge.model.Offer;
import com.c51.sedwards.c51challenge.model.OfferList;
import com.c51.sedwards.c51challenge.viewmodel.TestVals;
import com.google.gson.Gson;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class OfferAdapterTest {

    InputStream mockInputStream = new ByteArrayInputStream(TestVals.JSON_STRING.getBytes());

    @Test
    public void testSortData() {
        RecyclerView mockRv = mock(RecyclerView.class);
        RecyclerView.AdapterDataObserver dataObserver = mock(RecyclerView.AdapterDataObserver.class);
        doNothing().when(dataObserver).onItemRangeMoved(anyInt(), anyInt(), anyInt());

        OfferList list = new Gson().fromJson(new InputStreamReader(mockInputStream), OfferList.class);
        final OfferAdapter adapterUnderTest = new OfferAdapter(new ArrayList<Offer>(list.getOffers()));
        adapterUnderTest.onAttachedToRecyclerView(mockRv);
        adapterUnderTest.registerAdapterDataObserver(dataObserver);

        //TODO: Still need to determine how to mock the AdapterDataObservable inside the Adapter while testing my adapter code
        adapterUnderTest.sort(OfferListActivity.SortType.CASH_BACK);
        assertEquals(adapterUnderTest.getOffers().size(), list.getOffers().size());
        Collections.sort(list.getOffers(), OfferAdapter.CASH_BACK_COMPARATOR);
        assertArrayEquals(adapterUnderTest.getOffers().toArray(), list.getOffers().toArray());
        assertThat(adapterUnderTest.getOffers().get(0).getCashBack(), new Matcher<Float>() {
            @Override
            public boolean matches(Object item) {
                return (Float) item >= adapterUnderTest.getOffers().get(1).getCashBack();
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {

            }

            @Override
            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {

            }

            @Override
            public void describeTo(Description description) {

            }
        });
    }
}