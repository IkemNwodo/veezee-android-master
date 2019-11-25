package cloud.veezee.android.utils;

import com.google.android.material.appbar.AppBarLayout
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewElevationAwareScrollListener(private var elevationView: AppBarLayout) : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy);

        if(!recyclerView.canScrollVertically(-1)) {
            // we have reached the top of the list
            elevationView.elevation = 0f
        } else {
            // we are not at the top yet
            elevationView.elevation = 50f
        }
    }
}