package edu.muiv.univapp.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentNotificationsListBinding

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

        if (savedInstanceState == null)
            notificationListViewModel.loadNotifications()
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

        postponeEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationListViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            notifications?.let {
                Log.i(TAG, "Got ${notifications.size} notifications")
                updateUI(notifications)
                startPostponedEnterTransition()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(notifications: List<Notification>) {
        adapter.submitList(notifications)
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
            tvDate.text = notification.date
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
