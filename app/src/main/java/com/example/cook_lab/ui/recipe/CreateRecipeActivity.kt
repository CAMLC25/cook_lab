package com.example.cook_lab.ui.recipe

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.Category
import com.example.cook_lab.data.repository.CreateRecipeViewModelFactory
import com.example.cook_lab.databinding.ActivityCreateRecipeBinding
import com.example.cook_lab.databinding.ItemIngredientCreateBinding
import com.example.cook_lab.databinding.ItemStepCreateBinding
import com.example.cook_lab.repository.CreateRecipeRepository
import com.example.cook_lab.viewmodel.CategoryViewModel
import com.example.cook_lab.viewmodel.CreateRecipeViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.collections.map

class CreateRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRecipeBinding
    private lateinit var createRecipeViewModel: CreateRecipeViewModel
    private val categoriesViewModel: CategoryViewModel by viewModels()

    private var mainImageUri: Uri? = null
    private val stepImageUris = mutableMapOf<Int, Uri?>()
    private val PICK_IMAGE_REQUEST = 100
    private val STORAGE_PERMISSION_REQUEST_CODE = 101
    private var currentImageViewId: Int = -1

    companion object {
        private const val TAG = "CreateRecipeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Khởi tạo ViewModel bằng ViewModelFactory
        val repository = CreateRecipeRepository(ApiClient.apiService)
        val factory = CreateRecipeViewModelFactory(repository)
        createRecipeViewModel = ViewModelProvider(this, factory).get(CreateRecipeViewModel::class.java)

        // Quan sát danh mục
        categoriesViewModel.categories.observe(this) { categories ->
            if (categories == null || categories.isEmpty()) {
                Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show()
                return@observe
            }
            loadCategories(categories)
        }

        // Quan sát lỗi
        createRecipeViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Lỗi: $error", Toast.LENGTH_SHORT).show()
        }

        // Quan sát khi tạo công thức thành công
        createRecipeViewModel.createRecipeResponse.observe(this, Observer { response ->
            if (response != null && response.success) {
                Toast.makeText(this, "Công thức tạo thành công!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Lỗi: ${response?.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Chọn ảnh chính
        binding.mainImagePreview.setOnClickListener {
            currentImageViewId = binding.mainImagePreview.id
            requestStoragePermission()
        }

        // Thêm nguyên liệu và bước mặc định
        addIngredientRow()
        addStepRow()

        // Xử lý thêm nguyên liệu và bước
        binding.addIngredientButton.setOnClickListener { addIngredientRow() }
        binding.addStepButton.setOnClickListener { addStepRow() }

        // Xử lý nút lưu công thức
        binding.saveButton.setOnClickListener { saveRecipe() }

        // Xử lý nút hủy
        binding.cancelButton.setOnClickListener { finish() }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openImagePicker()
            return
        }

        val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                if (currentImageViewId == binding.mainImagePreview.id) {
                    mainImageUri = imageUri
                    Glide.with(this).load(imageUri).into(binding.mainImagePreview)
                } else {
                    val stepImageView = binding.stepsContainer.findViewById<ImageView>(currentImageViewId)
                    stepImageUris[currentImageViewId] = imageUri
                    stepImageView.isVisible = true
                    Glide.with(this).load(imageUri).into(stepImageView)
                }
            }
        }
    }

    private fun addIngredientRow() {
        val ingredientBinding = ItemIngredientCreateBinding.inflate(LayoutInflater.from(this), binding.ingredientsContainer, false)
        ingredientBinding.removeIngredientButton.setOnClickListener {
            binding.ingredientsContainer.removeView(ingredientBinding.root)
        }
        binding.ingredientsContainer.addView(ingredientBinding.root)
    }

    private fun addStepRow() {
        val stepBinding = ItemStepCreateBinding.inflate(LayoutInflater.from(this), binding.stepsContainer, false)
        stepBinding.stepTitle.text = "Bước ${binding.stepsContainer.childCount + 1}"

        val uniqueImageViewId = View.generateViewId()
        stepBinding.stepImageView.id = uniqueImageViewId

        stepBinding.addStepImageButton.setOnClickListener {
            currentImageViewId = stepBinding.stepImageView.id
            requestStoragePermission()
        }

        stepBinding.removeStepButton.setOnClickListener {
            binding.stepsContainer.removeView(stepBinding.root)
            stepImageUris.remove(stepBinding.stepImageView.id)
            updateStepTitles()
        }

        stepBinding.root.tag = stepBinding
        binding.stepsContainer.addView(stepBinding.root)
    }

    private fun updateStepTitles() {
        for (i in 0 until binding.stepsContainer.childCount) {
            val stepView = binding.stepsContainer.getChildAt(i)
            val stepBinding = stepView.tag as? ItemStepCreateBinding
            stepBinding?.stepTitle?.text = "Bước ${i + 1}"
        }
    }

    private fun saveRecipe() {
        if (!validateFields()) return // Kiểm tra các trường

        try {
            val title = binding.titleEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim().ifEmpty { null }
            val cookTime = binding.cookTimeEditText.text.toString().trim()
            val servings = binding.servingsEditText.text.toString().trim()

            // Kiểm tra thông tin công thức
            if (title.isEmpty() || cookTime.isEmpty() || servings.isEmpty() || mainImageUri == null) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return
            }

            // Chuyển đổi các trường thành RequestBody
            val titleRequestBody = RequestBody.create(MultipartBody.FORM, title)
            val descriptionRequestBody = description?.let { RequestBody.create(MultipartBody.FORM, it) }
            val cookTimeRequestBody = RequestBody.create(MultipartBody.FORM, cookTime)
            val servingsRequestBody = RequestBody.create(MultipartBody.FORM, servings)

            // Tạo RequestBody cho nguyên liệu
            val ingredients = mutableListOf<RequestBody>()
            for (i in 0 until binding.ingredientsContainer.childCount) {
                val ingredientView = binding.ingredientsContainer.getChildAt(i)
                val ingredientBinding = ItemIngredientCreateBinding.bind(ingredientView)
                val ingredientName = ingredientBinding.ingredientEditText.text.toString().trim()
                if (ingredientName.isNotEmpty()) {
                    ingredients.add(RequestBody.create(MultipartBody.FORM, ingredientName))  // Đảm bảo List<RequestBody>
                }
            }

            // Tạo RequestBody cho bước làm
            val stepDescriptions = mutableListOf<RequestBody>()
            val stepImages = mutableListOf<MultipartBody.Part>()
            for (i in 0 until binding.stepsContainer.childCount) {
                val stepView = binding.stepsContainer.getChildAt(i)
                val stepBinding = stepView.tag as? ItemStepCreateBinding
                val stepDescription = stepBinding?.stepEditText?.text.toString().trim()
                val stepImageUri = stepImageUris[stepBinding?.stepImageView?.id]

                if (stepDescription.isNotEmpty()) {
                    stepDescriptions.add(RequestBody.create(MultipartBody.FORM, stepDescription))  // Đảm bảo List<RequestBody>
                    stepImageUri?.let {
                        val file = File(getRealPathFromURI(it))
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData("steps[][image]", file.name, requestFile)
                        stepImages.add(part)
                    }
                }
            }

            // Chuyển ảnh chính thành MultipartBody.Part
            val mainImagePart = MultipartBody.Part.createFormData(
                "image",
                File(getRealPathFromURI(mainImageUri!!)).name,
                File(getRealPathFromURI(mainImageUri!!)).asRequestBody("image/*".toMediaTypeOrNull())
            )

            // Gọi ViewModel để gửi yêu cầu
            createRecipeViewModel.createRecipe(
                titleRequestBody,
                descriptionRequestBody,
                categoryId = RequestBody.create(MultipartBody.FORM, categoriesViewModel.categories.value?.get(binding.categorySpinner.selectedItemPosition)?.id.toString()),
                cookTimeRequestBody,
                servingsRequestBody,
                ingredients,  // List<RequestBody>
                stepDescriptions,  // List<RequestBody>
                stepImages,
                mainImagePart
            )

        } catch (e: Exception) {
            Log.e(TAG, "saveRecipe: Lỗi khi lưu công thức: ${e.message}", e)
            Toast.makeText(this, "Lỗi khi lưu công thức: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun validateFields(): Boolean {
        val title = binding.titleEditText.text.toString().trim()
        val cookTime = binding.cookTimeEditText.text.toString().trim()
        val servings = binding.servingsEditText.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề công thức", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cookTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập thời gian nấu", Toast.LENGTH_SHORT).show()
            return false
        }

        if (servings.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số lượng khẩu phần", Toast.LENGTH_SHORT).show()
            return false
        }

        if (mainImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh chính cho công thức", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun loadCategories(categories: List<Category>) {
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(this, R.layout.spinner_item, categoryNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
    }
    private fun getRealPathFromURI(uri: Uri): String {
        val filePathColumn = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(filePathColumn[0])
                return it.getString(columnIndex)
            }
        }
        return uri.path ?: ""
    }

}
