/*
 * Copyright 2016-2017, Institute of Cybernetics at Tallinn University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ee.ioc.phon.android.speak.demo

import android.app.Activity
import android.content.ComponentName
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import ee.ioc.phon.android.speak.R
import ee.ioc.phon.android.speak.model.CallerInfo
import ee.ioc.phon.android.speak.utils.Utils
import ee.ioc.phon.android.speak.view.AbstractSpeechInputViewListener
import ee.ioc.phon.android.speak.view.SpeechInputView
import ee.ioc.phon.android.speechutils.editor.UtteranceRewriter
import ee.ioc.phon.android.speechutils.utils.IntentUtils
import ee.ioc.phon.android.speechutils.utils.JsonUtils
import org.json.JSONException

/**
 * Simple chat style interface, which demonstrates how to use SpeechInputView.
 *
 * TODO: each list item should have at least 3 components: spoken input,
 * pretty-printed output (JSON, or parts of it), formal output (JSON that can be executed)
 */
class ChatDemoActivity : Activity() {

    private val mMatches = ArrayList<String>()

    private var mPrefs: SharedPreferences? = null
    private var mRes: Resources? = null
    private var recentMessage = ""

    var recent: MenuItem? = null
    var enterText: EditText? = null
    val speechInputViewListener: SpeechInputView.SpeechInputViewListener
        get() = object : AbstractSpeechInputViewListener() {

            private var mRewriters: Iterable<UtteranceRewriter>? = null

            override fun onComboChange(language: String, service: ComponentName) {
                mRewriters = Utils.genRewriters(mPrefs, mRes, arrayOf("Base", "Commands"), language, service, componentName)
            }

            override fun onFinalResult(results: List<String>, bundle: Bundle) {
                if (!results.isEmpty()) {
                    val result = results[0]
                    //String resultPp = "voice command (the raw utterance)\n\n" + result;
                    mMatches.add(result)
                    updateListView(mMatches)
                    // TODO: store the JSON also in the list, so that it can be reexecuted later
                    IntentUtils.launchIfIntent(this@ChatDemoActivity, mRewriters, result)
                }
            }

            override fun onError(errorCode: Int) {
                mMatches.add("* ERROR: $errorCode")
                updateListView(mMatches)
            }

            override fun onStartListening() {
                // stopTts();
            }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_demo)

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        mRes = resources
        enterText = findViewById(R.id.entertext)

        val siv = findViewById<SpeechInputView>(R.id.vSpeechInputView)
        val callerInfo = CallerInfo(createExtras(), callingActivity)
        // TODO: review this
        siv.init(R.array.keysActivity, callerInfo, 0)
        siv.setListener(speechInputViewListener, null)

        (findViewById<ListView>(R.id.list_matches)).onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val entry = parent.adapter.getItem(position)
            startActivity(entry.toString())
        }
    }

    private fun startActivity(intentAsJson: String) {
        try {
            IntentUtils.startActivityIfAvailable(this, JsonUtils.createIntent(intentAsJson))
        } catch (e: JSONException) {
            toast(e.localizedMessage)
        }
    }

    private fun updateListView(list: List<String>) {
        (findViewById<ListView>(R.id.list_matches)).adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
    }

    private fun toast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private fun createExtras(): Bundle {
        val bundle = Bundle()
        bundle.putInt(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        return bundle
    }
    /**Author: Thomas Tribunella
     * override method that create the option menu in the top right
     *  conner of the screen*/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.text_shortcuts,menu)
        recent = menu?.findItem(R.id.recenttext)
        return super.onCreateOptionsMenu(menu)
    }
    /**Author: Thomas Tribunella
    * override method that adds functionality to menu items in drop down menu */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when(item.itemId){
                R.id.hello -> {
                    mMatches.add("Hello")
                    updateListView(mMatches)
                    return true
                }
                R.id.whatsup -> {
                    mMatches.add("What's up")
                    updateListView(mMatches)
                    return true
                }
                R.id.how -> {
                    mMatches.add("How are you doing?")
                    updateListView(mMatches)
                    return true
                }
                R.id.ok -> {
                    mMatches.add("Okay")
                    updateListView(mMatches)
                    return true
                }
                R.id.good -> {
                    mMatches.add("I am good!")
                    updateListView(mMatches)
                    return true
                }
                R.id.notgood -> {
                    mMatches.add("I am not doing so good...")
                    updateListView(mMatches)
                    return true
                }
                R.id.recenttext -> {

                    if (recentMessage ==""){
                        Toast.makeText(applicationContext,"Please enter a message!",Toast.LENGTH_LONG)
                    }else {
                        mMatches.add(recentMessage)
                        updateListView(mMatches)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    /**Author: Thomas Tribunella
     * onClick method that adds functionality to the send button*/
    fun send(view: View){
        val message: String = enterText!!.text.toString()
        if (message ==""){
            Toast.makeText(applicationContext,"Please enter a message!",Toast.LENGTH_LONG)
        }else {
            recentMessage = message
            recent?.title = message
            mMatches.add(message)
            updateListView(mMatches)
        }
    }
}