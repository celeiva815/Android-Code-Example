package social.laika.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import social.laika.app.R;
import social.laika.app.adapters.EventsAdapter;
import social.laika.app.listeners.EventsRefreshListener;
import social.laika.app.models.Event;
import social.laika.app.network.RequestManager;
import social.laika.app.network.VolleyManager;
import social.laika.app.responses.EventsResponse;
import social.laika.app.utils.PrefsManager;
import social.laika.app.utils.Tag;

public class EventsActivity extends ActionBarActivity {

    public static final String TAG = EventsActivity.class.getSimpleName();

    private int mIdLayout = R.layout.lk_swipe_refresh_activity;
    public List<Event> mEvents;
    public SwipeRefreshLayout mSwipeLayout;
    public LinearLayout mEmptyLinearLayout;
    public ListView mEventsListView;
    public EventsAdapter mEventsAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mIdLayout);
        setActivityView();

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.laika_red));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onStart() {

        if (mEventsListView.getCount() == 0) {

            mEmptyLinearLayout.setVisibility(View.VISIBLE);
            requestEvents(Tag.NONE, Tag.LIMIT, getApplicationContext());
        }

        super.onStart();
    }

    @Override
    public void onBackPressed() {

        Event.deleteAll();
        super.onBackPressed();
    }

    public void setActivityView() {

        mEvents = getEvents();

        mEmptyLinearLayout = (LinearLayout) findViewById(R.id.empty_view);
        mEventsListView = (ListView) findViewById(R.id.main_listview);
        mEventsAdapter = new EventsAdapter(getApplicationContext(),
                R.layout.lk_events_adapter, mEvents);

        mEventsListView.setAdapter(mEventsAdapter);
        mEventsListView.setItemsCanFocus(true);

        mEventsListView.setEmptyView(mEmptyLinearLayout);

        //if (!mIsFavorite)
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        EventsRefreshListener refreshListener = new EventsRefreshListener(this);

        mEventsListView.setOnScrollListener(refreshListener);
        onCreateSwipeRefresh(mSwipeLayout, refreshListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        if (!this.getClass().equals(MainActivity.class))
            getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void onCreateSwipeRefresh(SwipeRefreshLayout refreshLayout,
                                      SwipeRefreshLayout.OnRefreshListener listener) {

        refreshLayout.setOnRefreshListener(listener);
        refreshLayout.setColorScheme(
                R.color.laika_light_red, R.color.light_white_font,
                R.color.laika_dark_red, R.color.light_white_font);
        refreshLayout.setSize(SwipeRefreshLayout.LARGE);

    }

    private List<Event> getEvents() {

        return Event.getEvents();
    }

    public void requestEvents(int lastEventId, int limit, Context context) {

        Map<String, String> params = new HashMap<>();

        if (lastEventId > Tag.NONE) {
            params.put(Event.API_LAST_EVENT_ID, Integer.toString(lastEventId));

        }
        params.put(Event.API_LIMIT, Integer.toString(limit));

        EventsResponse response = new EventsResponse(this);

        Request eventsRequest = RequestManager.getRequest(params, RequestManager.ADDRESS_EVENTS,
                response, response, PrefsManager.getUserToken(context));

        VolleyManager.getInstance(context)
                .addToRequestQueue(eventsRequest, TAG);

    }

    public void refreshList() {

        mEmptyLinearLayout.setVisibility(View.GONE);

        if (!mEvents.isEmpty()) {
            mEvents.clear();

        }

        mEvents.addAll(getEvents());
        mEventsAdapter.notifyDataSetChanged();

    }


}
