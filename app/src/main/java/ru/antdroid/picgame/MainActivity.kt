package ru.antdroid.picgame

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import ru.antdroid.picgame.PermissionUtils.hasPermissions
import java.io.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private val PERMISSION_STORAGE = 101
    private lateinit var imageView: ImageView
    private var mPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById<ImageView>(R.id.image)



        if (!PermissionUtils.hasPermissions(this))
            PermissionUtils.requestPermissions(this, PERMISSION_STORAGE)
        else
            loadData()


    }

    private fun loadData() {
        getImageSource()
        getSoundSource()

    }

    private fun getSoundSource(){
        val sdDir = Environment.getExternalStorageDirectory()

        val files = File("$sdDir/picgame/sounds")
        if (!files.exists()) {
            Toast.makeText(this, "Не найдена папка /picgame/sounds", Toast.LENGTH_LONG)
                .show()
            return
        }

        val list: Array<File>? = files.listFiles()

        var count = 0
        list?.forEach {
            val name: String = it.name
            if (name.endsWith(".mp3")) count++
        }

        if (list?.isEmpty() == true || count == 0) {
            Toast.makeText(this, "не нашли мы музычки в папке ((", Toast.LENGTH_LONG).show()
            return
        }

        val fileIndex = Random(System.currentTimeMillis()) .nextInt(0, count)
        val file = list?.get(fileIndex) ?: return

        imageView.setOnClickListener { playTheMusic(file) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PERMISSION_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (hasPermissions(this)) {
                    loadData()
                } else {
                    Toast.makeText(
                        this,
                        "Не имеем мы правоф на доступ к сему девайсу",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadData()
            } else {
                Toast.makeText(
                    this,
                    "Не имеем мы правоф на доступ к сему девайсу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getImageSource() {

        val sdDir = Environment.getExternalStorageDirectory()

        val files = File("$sdDir/picgame/picts")
        if (!files.exists()) {
            Toast.makeText(this, "Не найдена папка /picgame/picts", Toast.LENGTH_LONG)
                .show()
            return
        }
        val list: Array<File>? = files.listFiles()

        var count = 0
        list?.forEach {
            val name: String = it.name
            if (name.endsWith(".jpg")) count++
        }

        if (list?.isEmpty() == true || count == 0) {
            Toast.makeText(this, "не нашли мы картиночек в папке ((", Toast.LENGTH_LONG).show()
            return
        }

        val fileIndex = Random(System.currentTimeMillis()) .nextInt(0, count)
        val file = list?.get(fileIndex) ?: return

        loadPict(file)

    }

    private fun loadPict(file: File) {
        Glide.with(this)
            .asBitmap()
            .load(file)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Toast.makeText(
                        this@MainActivity,
                        "Чет нам не нравится файлик ${file.name}",
                        Toast.LENGTH_LONG
                    ).show()
                    return true
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    imageView.setImageBitmap(resource)
                    return true
                }

            }).submit()

    }

    override fun onPause() {
        super.onPause()
        mPlayer?.stop()
    }

    override fun onResume() {
        super.onResume()
        mPlayer?.prepare()
        mPlayer?.start()
    }

    private fun playTheMusic(file: File) {
        if(mPlayer == null){
            mPlayer = MediaPlayer.create(this, Uri.fromFile(file))
        }
        if(mPlayer?.isPlaying == true){
            mPlayer?.stop()
            mPlayer?.prepare()
            mPlayer?.start()
        }else{
            mPlayer?.start()
        }
    }
}