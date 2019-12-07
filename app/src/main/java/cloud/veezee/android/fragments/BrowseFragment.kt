package cloud.veezee.android.fragments

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import cloud.veezee.android.Constants
import cloud.veezee.android.R
import cloud.veezee.android.activities.LoginActivity
import cloud.veezee.android.activities.SettingActivity
import cloud.veezee.android.adapters.BrowseHorizontalListAdapter
import cloud.veezee.android.adapters.BrowseVerticalListAdapter
import cloud.veezee.android.api.API
import cloud.veezee.android.api.home
import cloud.veezee.android.api.utils.interfaces.HttpRequestListeners
import cloud.veezee.android.externalComponentsAndLibs.HidingScrollListener
import cloud.veezee.android.externalComponentsAndLibs.NestedScrollingParentRecyclerView
import cloud.veezee.android.models.HomePageItem
import cloud.veezee.android.models.PlayableItem
import cloud.veezee.android.utils.*
import cloud.veezee.android.utils.interfaces.OfflinePlayListResponseListener
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_home_page.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

class BrowseFragment : Fragment() {

    private val TAG: String = "BrowseFragment Console";

    //props
    private var homePageItems: ArrayList<HomePageItem> = ArrayList();
    private var adapter: BrowseVerticalListAdapter? = null;
    private var tabHeight = 0;
    private var cache: VeezeeCache? = null;

    //components
    private var browseList: NestedScrollingParentRecyclerView? = null;
    private var loading: ProgressBar? = null;

    private val offlinePlayListListener = object : OfflinePlayListResponseListener {
        override fun response(resource: String) {
            updateList(resource);
        }
    }

    private val volleyResponseListener = object : HttpRequestListeners.StringResponseListener {
        override fun response(response: String?) {
            if(response == null)
                return;

            cache?.cacheHomePageContent(response);
            updateList(response);
        }
    };

    private fun updateList(resource: String?) {
        if(homePageItems.size > 0)
            homePageItems.clear();

        homePageItems.addAll(Gson().fromJson(resource, object : TypeToken<ArrayList<HomePageItem>>() {}.type));
        adapter?.notifyDataSetChanged();

        (browseList as View).contentReadyToShow(true, loading);
    }

    private fun readContentFromCache(): String? {
        var contentJson: String? = null;

        if (cache!!.isContentCacheExist())
            contentJson = cache!!.retrieveHomePageContentJson();

        return contentJson;
    }

    private fun initializeList(): Boolean {
        tabHeight = resources.getDimension(R.dimen.tab_layout_height).toInt();

        browseList?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, LinearLayout.VERTICAL, false);
        //browseList?.addOnScrollListener(RecyclerViewCustomScrollListener(tabHeight));
        browseList?.adapter = adapter;
        OverScrollDecoratorHelper.setUpOverScroll(browseList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        return true;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater.inflate(R.layout.fragment_browse, container, false);
        //setHasOptionsMenu(true);

        browseList = view?.findViewById(R.id.browse_list);
        loading = view?.findViewById(R.id.browse_loading);
        cache = VeezeeCache(context!!);

        adapter = BrowseVerticalListAdapter(context!!, homePageItems);
        initializeList();

        if (isOnline(context)) {
            val contentJson = readContentFromCache();
            if(contentJson != null)
                updateList(contentJson);

            API.Lists.home(context!!, volleyResponseListener);
        } else {
            val playList: ArrayList<PlayableItem> = Couchbase.getInstance(context)?.getAll()!!;
            PlayListFactory(context!!).offlinePlayList(playList, offlinePlayListListener);
        }

        val appbarLayout: AppBarLayout? = activity?.findViewById(R.id.homePage_app_bar_layout);
        if(appbarLayout != null) {
            browseList?.addOnScrollListener(RecyclerViewElevationAwareScrollListener(appbarLayout));
        }

        return view;
    }
    
    /**override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu);

        try {
            if(Constants.GUEST_MODE == true) {
                val loginItem = menu?.findItem(R.id.login);
                loginItem?.isVisible = true;
            }
        } catch (ignored: Exception) { }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.setting -> {
                val settingActivity = Intent(context, SettingActivity::class.java);
                startActivity(settingActivity);
            }
            R.id.login -> {
                Constants.GUEST_MODE = null;
                val loginActivity = Intent(context, LoginActivity::class.java);
                startActivity(loginActivity);
            }
        }

        return super.onOptionsItemSelected(item);
    } */

   /** inner class RecyclerViewCustomScrollListener(tabHeight: Int) : HidingScrollListener(tabHeight) {
        override fun onShow() {
            val valueAnimation = ValueAnimator.ofInt(activity?.tab_layout?.height!!, tabHeight);
            valueAnimation.addUpdateListener { animation ->
                activity?.tab_layout?.layoutParams?.height = (animation?.animatedValue) as Int;
                activity?.tab_layout?.requestLayout();
            };
            valueAnimation.start();
        }

        override fun onHide() {

        }

        override fun onMoved(distance: Int) {
            activity?.tab_layout?.layoutParams?.height = tabHeight - distance;
            activity?.tab_layout?.requestLayout();
        }
    }*/
}