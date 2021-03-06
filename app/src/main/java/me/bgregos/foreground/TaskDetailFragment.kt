package me.bgregos.foreground

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.date_layout.view.*
import kotlinx.android.synthetic.main.task_detail.*
import kotlinx.android.synthetic.main.task_detail.view.*
import me.bgregos.foreground.task.LocalTasks
import me.bgregos.foreground.task.Task
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*


/**
 * A fragment representing a single Task detail screen.
 * This fragment is either contained in a [TaskListActivity]
 * in two-pane mode (on tablets) or a [TaskDetailActivity]
 * on handsets.
 */
class TaskDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: Task = Task("error")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = this.arguments

        //load Task from bundle args
        if (bundle!!.getString("uuid") != null) {
            item = LocalTasks.getTaskByUUID(UUID.fromString(bundle.getString("uuid")))?: Task("Task lookup error")
        }
        else {
            Log.e(this.javaClass.toString(), "No key found.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.task_detail, container, false)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        dateFormat.timeZone= TimeZone.getDefault()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        timeFormat.timeZone= TimeZone.getDefault()

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this.context ?: this.activity!!.baseContext,
                R.array.priority_spinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        rootView.detail_priority.adapter = adapter

        //load from Task into input fields
        item.let {
            rootView.detail_name.setText(it.name)
            if (it.priority != null) {
                rootView.detail_priority.setSelection(adapter.getPosition(it.priority))
            } else {
                rootView.detail_priority.setSelection(1)
            }

            rootView.detail_project.setText(it.project ?: "")
            val builder = StringBuilder()
            it.tags.forEach { task -> builder.append("$task, ") }
            rootView.detail_tags.setText(builder.toString().trimEnd(',', ' '))
            it.tags.removeAll { tag -> tag.isBlank() }
            it.tags = it.tags.map{ tag -> tag.trim() } as ArrayList<String>


            if(it.dueDate != null){
                rootView.detail_dueDate.date.setText(dateFormat.format(toLocal(it.dueDate as Date)))
                rootView.detail_dueDate.time.setText(timeFormat.format(toLocal(it.dueDate as Date)))
            }else{
                rootView.detail_dueDate.date.setText("")
                rootView.detail_dueDate.time.setText("")
            }
            if(it.waitDate != null){
                rootView.detail_waitDate.date.setText(dateFormat.format(toLocal(it.waitDate as Date)))
                rootView.detail_waitDate.time.setText(timeFormat.format(toLocal(it.waitDate as Date)))
            }else{
                rootView.detail_waitDate.date.setText("")
                rootView.detail_waitDate.time.setText("")
            }
        }

        rootView.detail_waitDate.dateInputLayout.hint = "Wait Until Date"
        rootView.detail_waitDate.date.setOnClickListener {
            saveAndUpdateTaskList()
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(this.requireContext(), DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in textbox
                detail_waitDate.date.setText(DateFormatSymbols().getMonths()[monthOfYear] + " " + dayOfMonth + ", " + year)
                if(detail_waitDate.time.text.isNullOrBlank()){
                    detail_waitDate.time.setText("12:00 AM")
                }
            }, year, month, day)
            dpd.show()
        }

        rootView.detail_waitDate.time.setOnClickListener {
            saveAndUpdateTaskList()
            val c = Calendar.getInstance()
            val hour:Int = c.get(Calendar.HOUR_OF_DAY)
            val minute:Int = c.get(Calendar.MINUTE)
            val dpd = TimePickerDialog(this.requireContext(), TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                // Display Selected date in textbox
                val inputFormat = SimpleDateFormat("KK:mm", Locale.getDefault())
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                detail_waitDate.time.setText(outputFormat.format(inputFormat.parse(""+hour+":"+minute)))
                if(detail_waitDate.date.text.isNullOrBlank()){
                    val year = c.get(Calendar.YEAR)
                    val month = c.get(Calendar.MONTH)
                    val day = c.get(Calendar.DAY_OF_MONTH)
                    detail_waitDate.date.setText(DateFormatSymbols().shortMonths[month] + " " + day + ", " + year)
                }
            }, hour, minute, false)
            dpd.show()
        }

        rootView.detail_dueDate.dateInputLayout.hint = "Due Date"
        rootView.detail_dueDate.date.setOnClickListener {
            saveAndUpdateTaskList()
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(this.requireContext(), { _, year, monthOfYear, dayOfMonth ->
                // Display Selected date in textbox
                detail_dueDate.date.setText("${DateFormatSymbols().months[monthOfYear]} $dayOfMonth, $year")
                if(detail_dueDate.time.text.isNullOrBlank()){
                    detail_dueDate.time.setText("12:00 AM")
                }
            }, year, month, day)
            dpd.show()
        }

        rootView.detail_dueDate.time.setOnClickListener {
            saveAndUpdateTaskList()
            val c = Calendar.getInstance()
            val hour:Int = c.get(Calendar.HOUR_OF_DAY)
            val minute:Int = c.get(Calendar.MINUTE)
            val dpd = TimePickerDialog(this.requireContext(), TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                // Display Selected date in textbox
                val inputFormat = SimpleDateFormat("KK:mm", Locale.getDefault())
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                detail_dueDate.time.setText(outputFormat.format(inputFormat.parse(""+hour+":"+minute)))
                if(detail_dueDate.date.text.isNullOrBlank()){
                    val year = c.get(Calendar.YEAR)
                    val month = c.get(Calendar.MONTH)
                    val day = c.get(Calendar.DAY_OF_MONTH)
                    detail_dueDate.date.setText("${DateFormatSymbols().shortMonths[month]} $day, $year")
                }
            }, hour, minute, false)
            dpd.show()
        }

        //some strange data race-ish issue requires this to be done late or the view doesn't adjust
        //to the match content height
        if (item.priority == null || item.priority == "No Priority Assigned"){
            rootView.detail_priority.setSelection(0)
            rootView.detail_priority.setSelection(1)
            rootView.detail_priority.setSelection(0)
            rootView.detail_priority.setSelection(1)
            rootView.detail_priority.setSelection(0)
        }

        return rootView
    }

    fun toLocal(date:Date):Date{
        val dfLocal = SimpleDateFormat()
        dfLocal.setTimeZone(TimeZone.getDefault())
        val dfUtc = SimpleDateFormat()
        dfUtc.setTimeZone(TimeZone.getTimeZone("UTC"))
        return dfUtc.parse(dfLocal.format(date))
    }

    fun toUtc(date:Date):Date{
        val dfLocal = SimpleDateFormat()
        dfLocal.setTimeZone(TimeZone.getDefault())
        val dfUtc = SimpleDateFormat()
        dfUtc.setTimeZone(TimeZone.getTimeZone("UTC"))
        return dfLocal.parse(dfUtc.format(date))
    }

    // This allows side-by-side tablet support to work
    private fun saveAndUpdateTaskList(){
        save()
        val localIntent: Intent = Intent("BRIGHTTASK_LOCAL_TASK_UPDATE") //Send local broadcast
        context?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(localIntent) }
        Log.d("detail", "sent update broadcast")
    }

    override fun onPause() {
        //handle save
        saveAndUpdateTaskList()
        super.onPause()
    }

    private fun save(){
        if (detail_name.text.toString().isBlank() || detail_name.text.toString().isEmpty()) {
            Log.d(this.javaClass.toString(), "Removing task w/ no name")
            val pos = LocalTasks.items.indexOf(item)
            LocalTasks.items.removeAt(pos)
            LocalTasks.save(activity!!.applicationContext)
            super.onPause()
        } else {
            val format = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            format.timeZone= TimeZone.getDefault()
            val toModify: Task = LocalTasks.getTaskByUUID(item.uuid) ?: throw NullPointerException("Task uuid lookup failed!")
            toModify.name=detail_name.text.toString()
            toModify.tags=detail_tags.text?.split(", ",",") as ArrayList<String>
            toModify.tags.removeAll { tag -> tag.isBlank() }
            toModify.project=detail_project.text?.toString()
            toModify.priority=detail_priority.selectedItem.toString()
            if (detail_priority.selectedItem.toString() == "No Priority Assigned") {
                toModify.priority = null
            }
            toModify.modifiedDate=toUtc(Date()) //update modified date
            if(!detail_dueDate.date.text.isNullOrBlank() && !detail_dueDate.time.text.isNullOrBlank()){
                toModify.dueDate=toUtc(format.parse("${detail_dueDate.date.text.toString()} ${detail_dueDate.time.text.toString()}"))
            }
            if(!detail_waitDate.date.text.isNullOrBlank() && !detail_waitDate.time.text.isNullOrBlank()){
                toModify.waitDate=toUtc(format.parse("${detail_waitDate.date.text.toString()} ${detail_waitDate.time.text.toString()}"))
            }
            val waitdate = toModify.waitDate
            if(waitdate != null && waitdate.after(Date())) {
                toModify.status ="waiting"
            }
            if (!LocalTasks.localChanges.contains(toModify)){
                LocalTasks.localChanges.add(toModify)
            }
            LocalTasks.save(activity!!.applicationContext, true)
            Log.d(this.javaClass.toString(), "Saved task")
            super.onPause()
        }
    }
}
