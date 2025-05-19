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
import com.example.cook_lab.data.repository.EditRecipeViewModelFactory
import com.example.cook_lab.databinding.ActivityEditRecipeBinding
import com.example.cook_lab.databinding.ItemIngredientCreateBinding
import com.example.cook_lab.databinding.ItemStepCreateBinding
import com.example.cook_lab.repository.CreateRecipeRepository
import com.example.cook_lab.viewmodel.CategoryViewModel
import com.example.cook_lab.viewmodel.EditRecipeViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecipeBinding
    private lateinit var editRecipeViewModel: EditRecipeViewModel
    private val categoriesViewModel: CategoryViewModel by viewModels()

    private var mainImageUri: Uri? = null
    private val stepImageUris = mutableMapOf<Int, Uri?>()
    private var recipeId: Int = -1
    private val PICK_IMAGE_REQUEST = 100
    private val STORAGE_PERMISSION_REQUEST_CODE = 101
    private var currentImageViewId: Int = -1

    companion object {
        private const val TAG = "EditRecipeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Nhận recipeId từ intent để tải thông tin công thức
        recipeId = intent.getIntExtra("RECIPE_ID", -1)

        if (recipeId == -1) {
            Toast.makeText(this, "Công thức không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Khởi tạo ViewModel bằng ViewModelFactory
        val repository = CreateRecipeRepository(ApiClient.apiService)
        val factory = EditRecipeViewModelFactory(repository)
        editRecipeViewModel = ViewModelProvider(this, factory).get(EditRecipeViewModel::class.java)

        // Quan sát danh mục
        categoriesViewModel.categories.observe(this) { categories ->
            if (categories == null || categories.isEmpty()) {
                Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show()
                return@observe
            }
            loadCategories(categories)
        }

        // Quan sát lỗi
        editRecipeViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Lỗi: $error", Toast.LENGTH_SHORT).show()
        }

        // Quan sát khi cập nhật công thức thành công
        editRecipeViewModel.updateRecipeResponse.observe(this, Observer { response ->
            if (response != null && response.success) {
                Toast.makeText(this, "Cập nhật công thức thành công!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Lỗi: ${response?.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Tải công thức cần chỉnh sửa
        loadRecipeDetails()

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
        binding.saveButton.setOnClickListener { updateRecipe() }

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

    private fun loadRecipeDetails() {
        // Gọi API để lấy chi tiết
        editRecipeViewModel.getRecipeById(recipeId)

        // Quan sát LiveData
        editRecipeViewModel.recipe.observe(this, Observer { recipe ->
            recipe?.let {
                // --- 1. Ảnh chính ---
                Glide.with(this)
                    .load(it.image) // Đảm bảo API trả về URL đúng
                    .placeholder(R.drawable.start_activity) // Placeholder khi ảnh chưa tải
                    .error(R.drawable.start_activity) // Nếu lỗi khi tải ảnh
                    .into(binding.mainImagePreview) // Gán vào ImageView
                mainImageUri = Uri.parse(it.image) //
                Log.e("EditRecipeActivity_1", "Image URL: ${mainImageUri}") // Lưu URI cho việc update sau này

                // --- 2. Tiêu đề, mô tả, cookTime, servings ---
                binding.titleEditText.setText(it.title)
                binding.descriptionEditText.setText(it.description)
                binding.cookTimeEditText.setText(it.cook_time.toString())
                binding.servingsEditText.setText(it.servings.toString())

                // --- 3. Spinner danh mục ---
                val categories = categoriesViewModel.categories.value ?: emptyList()
                val idx = categories.indexOfFirst { cat -> cat.id == it.category_id }
                if (idx >= 0) binding.categorySpinner.setSelection(idx)

                // --- 4. Nguyên liệu ---
                binding.ingredientsContainer.removeAllViews()
                it.ingredients.forEach { ingredient ->
                    val ingBind = ItemIngredientCreateBinding.inflate(
                        layoutInflater, binding.ingredientsContainer, false
                    )
                    ingBind.ingredientEditText.setText(ingredient.name)
                    ingBind.removeIngredientButton.setOnClickListener {
                        binding.ingredientsContainer.removeView(ingBind.root)
                    }
                    binding.ingredientsContainer.addView(ingBind.root)
                }
                // Nếu không có nguyên liệu nào, vẫn giữ 1 hàng trống để user thêm
                if (it.ingredients.isEmpty()) addIngredientRow()

                // --- 5. Các bước làm ---
                binding.stepsContainer.removeAllViews()
                it.steps.forEachIndexed { index, step ->
                    val stepBind = ItemStepCreateBinding.inflate(
                        layoutInflater, binding.stepsContainer, false
                    )
                    stepBind.stepTitle.text = "Bước ${index + 1}"
                    stepBind.stepEditText.setText(step.description)

                    // Nếu bước có ảnh, load vào ImageView
                    if (!step.image.isNullOrEmpty()) {
                        stepBind.stepImageView.isVisible = true
                        val imageUrl = ApiClient.BASE_URL + step.image
                        Glide.with(this)
                            .load(imageUrl) // Tải ảnh từ URL
                            .placeholder(R.drawable.placeholder) // Placeholder
                            .error(R.drawable.placeholder) // Nếu lỗi khi tải ảnh
                            .into(stepBind.stepImageView)
                        // Lưu URI để gửi lại ảnh này khi update
                        stepImageUris[stepBind.stepImageView.id] = Uri.parse(imageUrl)
                        Log.e("EditRecipeActivity", "Image URL: ${imageUrl}")
                        Log.e("EditRecipeActivity", "Image URI: ${stepImageUris[stepBind.stepImageView.id]}")

                    }

                    stepBind.addStepImageButton.setOnClickListener {
                        currentImageViewId = stepBind.stepImageView.id
                        requestStoragePermission()
                    }

                    stepBind.removeStepButton.setOnClickListener {
                        binding.stepsContainer.removeView(stepBind.root)
                        stepImageUris.remove(stepBind.stepImageView.id)
                        updateStepTitles()
                    }

                    stepBind.root.tag = stepBind
                    binding.stepsContainer.addView(stepBind.root)
                }

                // Nếu không có bước nào, vẫn giữ 1 bước trống
                if (it.steps.isEmpty()) addStepRow()
            }
        })
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

    private fun updateRecipe() {
        if (!validateFields()) return

        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
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
            Log.e("EditRecipeActivity_123", "Step Image URI: ${stepImageUri}")

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

        // Gọi ViewModel để gửi yêu cầu cập nhật
        editRecipeViewModel.updateRecipe(
            recipeId,
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