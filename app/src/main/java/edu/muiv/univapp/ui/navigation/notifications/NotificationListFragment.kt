package edu.muiv.univapp.ui.navigation.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentNotificationsListBinding
import edu.muiv.univapp.utils.FetchedListType

class NotificationListFragment : Fragment() {

    companion object {
        private const val TAG = "NotificationLF"
    }

    private var _binding: FragmentNotificationsListBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val notificationListViewModel by lazy {
        ViewModelProvider(this)[NotificationListViewModel::class.java]
    }

    private lateinit var rvNotifications: RecyclerView
    private var adapter = NotificationAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            notificationListViewModel.loadNotifications()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsListBinding.inflate(inflater, container, false)
        val view = binding.root

        rvNotifications = binding.rvNotifications
        rvNotifications.layoutManager = LinearLayoutManager(context)
        rvNotifications.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (notificationListViewModel.isTeacher) {
            notificationListViewModel.notificationsForTeacher.observe(viewLifecycleOwner) { notifications ->
                Log.i(TAG, "Got ${notifications.size} notifications for teacher")
                updateUI(notifications)
            }
        } else {
            notificationListViewModel.notificationsForStudent.observe(viewLifecycleOwner) { notifications ->
                notifications?.let {
                    Log.i(TAG, "Got ${notifications.size} notifications for student")
                    updateUI(notifications)
                }
            }
            notificationListViewModel.fetchedNotifications.observe(viewLifecycleOwner) { response ->

                /**
                 * Response codes ->
                 * 204: No [Notification]
                 * 200: Got [Notification]
                 * 500: Server failure response
                 * 503: Service is unavailable
                 */

                val responseCode = response.keys.first()
                val notifications = response.values.first()
                val textTemplate = "Fetched notifications: "
                when (responseCode) {
                    204 -> Log.i(TAG, "$textTemplate Haven't got any notifications ($responseCode)")
                    503 -> Log.w(TAG, "$textTemplate Server isn't working ($responseCode)")
                    500 -> Log.e(TAG, "$textTemplate Got unexpected fail ($responseCode)")
                    200 -> {
                        Log.i(TAG, "Trying to update database with fetched notifications...")

                        // Update database with fetched notifications
                        notificationListViewModel.upsertNotifications(notifications!!)

                        // Create a list with ids of fetched notifications
                        notificationListViewModel.createNotificationsIdList(
                            notifications, FetchedListType.NEW.type
                        )

                        // Prevent from endless updates
                        notificationListViewModel.fetchedNotifications.removeObservers(viewLifecycleOwner)
                    }
                }
            }
        }

        // Wait for animations availability
        postponeEnterTransition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(notifications: List<Notification>) {
        adapter.submitList(notifications)

        // Create a list of queried notifications ids
        notificationListViewModel.createNotificationsIdList(
            notifications, FetchedListType.OLD.type
        )

        // Appearance of the items
        rvNotifications.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                rvNotifications.viewTreeObserver.removeOnPreDrawListener(this)

                for (view in rvNotifications.children) {
                    view.alpha = 0f
                    view.animate().alpha(1f)
                        .setDuration(300)
                        .start()
                }

                return true
            }
        })

        // Allow animations to play
        startPostponedEnterTransition()
    }

    private inner class NotificationAdapter
        : ListAdapter<Notification, NotificationHolder>(DiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
            val view = layoutInflater.inflate(R.layout.notification_list_item, parent, false)

            return NotificationHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
            val notification = currentList[position]
            holder.bind(notification)
        }
    }

    private inner class NotificationHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvText: TextView = itemView.findViewById(R.id.tvText)

        fun bind(notification: Notification) {
            tvDate.text = notificationListViewModel.getSimpleDate(notification.date)
            tvTitle.text = notification.title
            tvText.text = notification.text
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}
