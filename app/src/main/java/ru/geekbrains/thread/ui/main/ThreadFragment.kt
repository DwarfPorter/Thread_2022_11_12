package ru.geekbrains.thread.ui.main

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.*
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import ru.geekbrains.thread.R
import ru.geekbrains.thread.databinding.FragmentThreadBinding
import java.util.*
import java.util.concurrent.TimeUnit

const val TEST_BROADCAST_INTENT_FILTER = "TEST BROADCAST INTENT FILTER"
const val THREADS_FRAGMENT_BROADCAST_EXTRA = "THREADS_FRAGMENT_EXTRA"

class ThreadFragment : Fragment() {

    private var counterThread = 0

    private var _binding: FragmentThreadBinding? = null
    private val binding get() = _binding!!

    //Создаём свой BroadcastReceiver (получатель широковещательного сообщения)
    private val testReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Достаём данные из интента
            intent.getStringExtra(THREADS_FRAGMENT_BROADCAST_EXTRA)?.let {
                binding.mainContainer.addView(AppCompatTextView(context).apply {
                    text = it
                    textSize = resources.getDimension(R.dimen.main_container_text_size)
                })
            }
        }
    }

    companion object {
        fun newInstance() = ThreadFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThreadBinding.inflate(inflater, container, false)
        context?.registerReceiver(testReceiver, IntentFilter(TEST_BROADCAST_INTENT_FILTER))
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            binding.textView.text = startCalculations(binding.editText.text.toString().toInt())
            binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                text = getString(R.string.in_main_thread)
                textSize = resources.getDimension(R.dimen.main_container_text_size)
            })
        }
        binding.calcThreadBtn.setOnClickListener {
            Thread {
                counterThread++;
                val cntrThread = counterThread;
                val calculatedText = startCalculations(binding.editText.text.toString().toInt())
                activity?.runOnUiThread {
                    binding.textView.text = calculatedText
                    binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                        text = String.format(getString(R.string.from_thread), cntrThread)
                        textSize = resources.getDimension(R.dimen.main_container_text_size)
                    })
                }
            }.start()
        }

        val handlerThread = HandlerThread(getString(R.string.my_handler_thread))
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        binding.calcThreadHandler.setOnClickListener {
            counterThread++
            val cntrThread = counterThread
            binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                text = String.format(
                    getString(R.string.calculate_in_handler_thread),
                    handlerThread.name + " " + cntrThread
                )
                textSize = resources.getDimension(R.dimen.main_container_text_size)
            })

            handler.post {
                startCalculations(binding.editText.text.toString().toInt())
                binding.mainContainer.post {
                    binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                        text = String.format(
                            getString(R.string.return_from_handler_thread),
                            Thread.currentThread().name + " " + cntrThread
                        )
                        textSize = resources.getDimension(R.dimen.main_container_text_size)
                    })
                }
            }
        }

        binding.jobServiceButton.setOnClickListener {
            runService()
        }

        binding.serviceButton.setOnClickListener {
            context?.let {
                it.startService(Intent(it, MyIntentService::class.java).apply {
                    putExtra(
                        DURATION,
                        binding.editText.text.toString().toInt()
                    )
                })
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun runService() {
        val bundle = PersistableBundle().apply {
            putInt(DURATION, binding.editText.text.toString().toInt())
        }

        val builderMyJobInfo =
            JobInfo.Builder(123, ComponentName(requireContext(), MyJobService::class.java))
                .setExtras(bundle)
                .setMinimumLatency(1000) // wait at least
                .setOverrideDeadline(100000) // maximum delay

        val myJobScheduler = requireContext().getSystemService(JobScheduler::class.java)
        myJobScheduler.schedule(builderMyJobInfo.build())
    }

    private fun startCalculations(seconds: Int): String {
        val date = Date()
        var diffInSec: Long
        do {
            val currentDate = Date()
            val diffInMs: Long = currentDate.time - date.time
            diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs)
        } while (diffInSec < seconds)
        return diffInSec.toString()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        context?.unregisterReceiver(testReceiver)
        _binding = null;
    }

}