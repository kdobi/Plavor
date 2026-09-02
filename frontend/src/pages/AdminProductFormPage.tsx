import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  createAdminProduct,
  fetchAdminProduct,
  updateAdminProduct,
  uploadAdminProductImage,
} from '../api/admin'
import { ApiError } from '../api/auth'
import { fetchCategories } from '../api/catalog'
import { useAuth } from '../auth/auth-state'
import { AdminAccessGate } from '../components/AdminAccessGate'
import {
  AdminBreadcrumb,
  AdminNavigation,
} from '../components/AdminNavigation'
import { SiteHeader } from '../components/SiteHeader'
import type { AdminProduct, AdminProductRequest } from '../types/admin'
import type { Category, ProductStatus } from '../types/catalog'
import { currencyFormatter, formatImageUrl } from '../utils/catalog'

type AdminProductImageForm = {
  key: string
  imageUrl: string
  altText: string
  displayOrder: string
  thumbnail: boolean
}

type AdminProductForm = {
  categoryId: string
  name: string
  description: string
  price: string
  stockQuantity: string
  status: ProductStatus
  images: AdminProductImageForm[]
}

type ProductFormErrors = Partial<Record<keyof AdminProductForm, string>>

const MAX_IMAGE_COUNT = 10
const PRODUCT_STATUS_OPTIONS: Array<{
  label: string
  value: ProductStatus
}> = [
  { label: '판매중', value: 'ACTIVE' },
  { label: '품절', value: 'SOLD_OUT' },
  { label: '숨김', value: 'HIDDEN' },
]

const INITIAL_FORM: AdminProductForm = {
  categoryId: '',
  name: '',
  description: '',
  price: '',
  stockQuantity: '',
  status: 'ACTIVE',
  images: [createImageForm(true)],
}

export function AdminProductFormPage() {
  const { productId } = useParams()
  const navigate = useNavigate()
  const { accessToken, user } = useAuth()
  const [categories, setCategories] = useState<Category[]>([])
  const [form, setForm] = useState<AdminProductForm>(INITIAL_FORM)
  const [fieldErrors, setFieldErrors] = useState<ProductFormErrors>({})
  const [message, setMessage] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [uploadingImageKey, setUploadingImageKey] = useState<string | null>(
    null,
  )

  const isEditMode = Boolean(productId)
  const canLoad = Boolean(accessToken && user?.role === 'ADMIN')

  useEffect(() => {
    if (!accessToken || !canLoad) {
      return
    }

    const token = accessToken
    const controller = new AbortController()

    async function loadFormData() {
      setIsLoading(true)
      setMessage('')

      try {
        const [categoryData, productData] = await Promise.all([
          fetchCategories(controller.signal),
          productId
            ? fetchAdminProduct(token, productId, controller.signal)
            : Promise.resolve(null),
        ])

        setCategories(categoryData)

        if (productData) {
          setForm(mapProductToForm(productData))
        } else {
          setForm(INITIAL_FORM)
        }
      } catch (error) {
        if (!controller.signal.aborted) {
          setMessage(readApiMessage(error, '상품 정보를 불러오지 못했습니다.'))
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    loadFormData()

    return () => controller.abort()
  }, [accessToken, canLoad, productId])

  const thumbnailPreview = useMemo(() => {
    return form.images.find((image) => image.thumbnail && image.imageUrl.trim())
      ?? form.images.find((image) => image.imageUrl.trim())
      ?? null
  }, [form.images])

  const pricePreview = Number(form.price || 0)

  function handleFieldChange<K extends keyof AdminProductForm>(
    field: K,
    value: AdminProductForm[K],
  ) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }))
    setFieldErrors((current) => clearFieldError(current, field))
  }

  function handleImageChange(
    key: string,
    field: keyof AdminProductImageForm,
    value: string | boolean,
  ) {
    setForm((current) => ({
      ...current,
      images: current.images.map((image) => {
        if (image.key !== key) {
          return image
        }

        return {
          ...image,
          [field]: value,
        }
      }),
    }))
    setFieldErrors((current) => clearFieldError(current, 'images'))
  }

  async function handleImageUpload(key: string, files: FileList | null) {
    const file = files?.[0]
    if (!file || !accessToken) {
      return
    }

    setUploadingImageKey(key)
    setMessage('')

    try {
      const uploadedImage = await uploadAdminProductImage(accessToken, file)

      setForm((current) => {
        const fallbackAltText =
          current.name.trim()
          || removeFileExtension(uploadedImage.originalFilename ?? file.name)

        return {
          ...current,
          images: current.images.map((image) => {
            if (image.key !== key) {
              return image
            }

            return {
              ...image,
              imageUrl: uploadedImage.imageUrl,
              altText: image.altText || fallbackAltText,
            }
          }),
        }
      })
      setFieldErrors((current) => clearFieldError(current, 'images'))
    } catch (error) {
      setMessage(readApiMessage(error, '이미지를 업로드하지 못했습니다.'))
    } finally {
      setUploadingImageKey(null)
    }
  }

  function handleThumbnailChange(key: string) {
    setForm((current) => ({
      ...current,
      images: current.images.map((image) => ({
        ...image,
        thumbnail: image.key === key,
      })),
    }))
    setFieldErrors((current) => clearFieldError(current, 'images'))
  }

  function handleAddImage() {
    setForm((current) => {
      if (current.images.length >= MAX_IMAGE_COUNT) {
        return current
      }

      return {
        ...current,
        images: [...current.images, createImageForm(current.images.length === 0)],
      }
    })
  }

  function handleRemoveImage(key: string) {
    setForm((current) => {
      const nextImages = current.images.filter((image) => image.key !== key)

      if (nextImages.length === 0) {
        return {
          ...current,
          images: [createImageForm(true)],
        }
      }

      if (!nextImages.some((image) => image.thumbnail)) {
        return {
          ...current,
          images: nextImages.map((image, index) => ({
            ...image,
            thumbnail: index === 0,
          })),
        }
      }

      return {
        ...current,
        images: nextImages,
      }
    })
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!accessToken) {
      return
    }

    const nextErrors = validateForm(form)
    setFieldErrors(nextErrors)

    if (Object.keys(nextErrors).length > 0) {
      return
    }

    setIsSaving(true)
    setMessage('')

    try {
      const request = mapFormToRequest(form)

      if (isEditMode && productId) {
        await updateAdminProduct(accessToken, productId, request)
      } else {
        await createAdminProduct(accessToken, request)
      }

      navigate('/admin/products')
    } catch (error) {
      setMessage(readApiMessage(error, '상품을 저장하지 못했습니다.'))
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="storefront">
      <SiteHeader />

      <main className="admin-page">
        <AdminAccessGate>
          <section className="admin-heading">
            <div>
              <AdminBreadcrumb current={isEditMode ? '상품 수정' : '새 상품 등록'} />
              <h1>{isEditMode ? '상품 수정' : '새 상품 등록'}</h1>
            </div>
            <Link className="admin-secondary-link" to="/admin/products">
              목록으로
            </Link>
          </section>

          <AdminNavigation active="products" />

          {isLoading ? (
            <div className="admin-form-skeleton" />
          ) : (
            <form className="admin-form-layout" onSubmit={handleSubmit}>
              <section className="admin-form-panel">
                <div className="admin-form-section">
                  <h2>기본 정보</h2>

                  <label className="admin-field">
                    <span>카테고리</span>
                    <select
                      value={form.categoryId}
                      aria-invalid={Boolean(fieldErrors.categoryId)}
                      onChange={(event) =>
                        handleFieldChange('categoryId', event.target.value)
                      }
                    >
                      <option value="">카테고리 선택</option>
                      {categories.map((category) => (
                        <option key={category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                    </select>
                    {fieldErrors.categoryId && (
                      <small>{fieldErrors.categoryId}</small>
                    )}
                  </label>

                  <label className="admin-field">
                    <span>상품명</span>
                    <input
                      type="text"
                      value={form.name}
                      maxLength={200}
                      placeholder="Minimal Cotton T-Shirt"
                      aria-invalid={Boolean(fieldErrors.name)}
                      onChange={(event) =>
                        handleFieldChange('name', event.target.value)
                      }
                    />
                    {fieldErrors.name && <small>{fieldErrors.name}</small>}
                  </label>

                  <label className="admin-field">
                    <span>설명</span>
                    <textarea
                      value={form.description}
                      maxLength={5000}
                      placeholder="상품의 소재, 핏, 관리 방법 등을 입력하세요."
                      onChange={(event) =>
                        handleFieldChange('description', event.target.value)
                      }
                    />
                  </label>

                  <div className="admin-field-grid">
                    <label className="admin-field">
                      <span>가격</span>
                      <input
                        type="text"
                        inputMode="numeric"
                        value={form.price}
                        placeholder="29000"
                        aria-invalid={Boolean(fieldErrors.price)}
                        onChange={(event) =>
                          handleFieldChange(
                            'price',
                            normalizeNumber(event.target.value),
                          )
                        }
                      />
                      {fieldErrors.price && <small>{fieldErrors.price}</small>}
                    </label>

                    <label className="admin-field">
                      <span>재고</span>
                      <input
                        type="text"
                        inputMode="numeric"
                        value={form.stockQuantity}
                        placeholder="120"
                        aria-invalid={Boolean(fieldErrors.stockQuantity)}
                        onChange={(event) =>
                          handleFieldChange(
                            'stockQuantity',
                            normalizeNumber(event.target.value),
                          )
                        }
                      />
                      {fieldErrors.stockQuantity && (
                        <small>{fieldErrors.stockQuantity}</small>
                      )}
                    </label>
                  </div>

                  <label className="admin-field">
                    <span>판매 상태</span>
                    <select
                      value={form.status}
                      onChange={(event) =>
                        handleFieldChange(
                          'status',
                          event.target.value as ProductStatus,
                        )
                      }
                    >
                      {PRODUCT_STATUS_OPTIONS.map((status) => (
                        <option key={status.value} value={status.value}>
                          {status.label}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                <div className="admin-form-section">
                  <div className="admin-section-title-row">
                    <h2>이미지</h2>
                    <button
                      type="button"
                      disabled={form.images.length >= MAX_IMAGE_COUNT}
                      onClick={handleAddImage}
                    >
                      이미지 추가
                    </button>
                  </div>

                  {fieldErrors.images && (
                    <p className="admin-inline-message">{fieldErrors.images}</p>
                  )}

                  <div className="admin-image-fields">
                    {form.images.map((image, index) => (
                      <fieldset className="admin-image-field" key={image.key}>
                        <legend>이미지 {index + 1}</legend>

                        <label className="admin-check-field">
                          <input
                            type="checkbox"
                            checked={image.thumbnail}
                            onChange={() => handleThumbnailChange(image.key)}
                          />
                          <span>대표 이미지</span>
                        </label>

                        <label className="admin-upload-field">
                          <span>이미지 파일</span>
                          <input
                            type="file"
                            accept="image/jpeg,image/png,image/webp"
                            disabled={uploadingImageKey === image.key}
                            onChange={(event) => {
                              const input = event.currentTarget
                              void handleImageUpload(
                                image.key,
                                input.files,
                              ).finally(() => {
                                input.value = ''
                              })
                            }}
                          />
                          <small className="admin-field-hint">
                            {uploadingImageKey === image.key
                              ? '이미지 업로드 중'
                              : 'jpg, png, webp 파일을 업로드할 수 있습니다.'}
                          </small>
                        </label>

                        <label className="admin-field">
                          <span>이미지 URL</span>
                          <input
                            type="text"
                            inputMode="url"
                            value={image.imageUrl}
                            maxLength={500}
                            placeholder="https://... 또는 /uploads/products/..."
                            onChange={(event) =>
                              handleImageChange(
                                image.key,
                                'imageUrl',
                                event.target.value,
                              )
                            }
                          />
                        </label>

                        <div className="admin-field-grid compact">
                          <label className="admin-field">
                            <span>대체 텍스트</span>
                            <input
                              type="text"
                              value={image.altText}
                              maxLength={255}
                              placeholder={form.name || '상품 이미지'}
                              onChange={(event) =>
                                handleImageChange(
                                  image.key,
                                  'altText',
                                  event.target.value,
                                )
                              }
                            />
                          </label>

                          <label className="admin-field">
                            <span>노출 순서</span>
                            <input
                              type="text"
                              inputMode="numeric"
                              value={image.displayOrder}
                              onChange={(event) =>
                                handleImageChange(
                                  image.key,
                                  'displayOrder',
                                  normalizeNumber(event.target.value),
                                )
                              }
                            />
                          </label>
                        </div>

                        <button
                          className="admin-danger-button"
                          type="button"
                          onClick={() => handleRemoveImage(image.key)}
                        >
                          삭제
                        </button>
                      </fieldset>
                    ))}
                  </div>
                </div>
              </section>

              <aside className="admin-preview-panel">
                <h2>미리보기</h2>
                <div className="admin-preview-image">
                  {thumbnailPreview?.imageUrl ? (
                    <img
                      src={formatImageUrl(thumbnailPreview.imageUrl, 520)}
                      alt=""
                    />
                  ) : (
                    <span>PLAVOR</span>
                  )}
                </div>
                <div className="admin-preview-copy">
                  <p>{selectedCategoryName(categories, form.categoryId)}</p>
                  <strong>{form.name || '상품명'}</strong>
                  <span>{currencyFormatter.format(pricePreview)}원</span>
                </div>

                {message && <p className="admin-message">{message}</p>}

                <button
                  type="submit"
                  disabled={isSaving || uploadingImageKey !== null}
                >
                  {isSaving ? '저장 중' : isEditMode ? '상품 수정하기' : '상품 등록하기'}
                </button>
              </aside>
            </form>
          )}
        </AdminAccessGate>
      </main>
    </div>
  )
}

function createImageForm(thumbnail = false): AdminProductImageForm {
  return {
    key: `${Date.now()}-${Math.random()}`,
    imageUrl: '',
    altText: '',
    displayOrder: '0',
    thumbnail,
  }
}

function mapProductToForm(product: AdminProduct): AdminProductForm {
  return {
    categoryId: String(product.category.id),
    name: product.name,
    description: product.description ?? '',
    price: String(product.price),
    stockQuantity: String(product.stockQuantity),
    status: product.status,
    images:
      product.images.length > 0
        ? product.images.map((image) => ({
            key: `${image.id}-${image.displayOrder}`,
            imageUrl: image.imageUrl,
            altText: image.altText ?? '',
            displayOrder: String(image.displayOrder),
            thumbnail: image.thumbnail,
          }))
        : [createImageForm(true)],
  }
}

function mapFormToRequest(form: AdminProductForm): AdminProductRequest {
  return {
    categoryId: Number(form.categoryId),
    name: form.name.trim(),
    description: form.description.trim() || null,
    price: Number(form.price),
    stockQuantity: Number(form.stockQuantity),
    status: form.status,
    images: form.images
      .filter((image) => image.imageUrl.trim())
      .map((image) => ({
        imageUrl: image.imageUrl.trim(),
        altText: image.altText.trim() || null,
        displayOrder: Number(image.displayOrder || 0),
        thumbnail: image.thumbnail,
      })),
  }
}

function validateForm(form: AdminProductForm): ProductFormErrors {
  const errors: ProductFormErrors = {}

  if (!form.categoryId) {
    errors.categoryId = '카테고리를 선택해주세요.'
  }

  if (!form.name.trim()) {
    errors.name = '상품명을 입력해주세요.'
  }

  if (form.price === '') {
    errors.price = '가격을 입력해주세요.'
  }

  if (form.stockQuantity === '') {
    errors.stockQuantity = '재고를 입력해주세요.'
  }

  if (form.status === 'ACTIVE' && Number(form.stockQuantity || 0) === 0) {
    errors.stockQuantity = '판매중 상품은 재고가 1개 이상이어야 합니다.'
  }

  const filledImages = form.images.filter((image) => image.imageUrl.trim())
  const thumbnailCount = filledImages.filter((image) => image.thumbnail).length

  if (filledImages.length > MAX_IMAGE_COUNT) {
    errors.images = `이미지는 최대 ${MAX_IMAGE_COUNT}개까지 등록할 수 있습니다.`
  }

  if (thumbnailCount > 1) {
    errors.images = '대표 이미지는 하나만 선택할 수 있습니다.'
  }

  return errors
}

function normalizeNumber(value: string) {
  return value.replace(/\D/g, '')
}

function removeFileExtension(filename: string) {
  const extensionIndex = filename.lastIndexOf('.')
  return extensionIndex > 0 ? filename.slice(0, extensionIndex) : filename
}

function clearFieldError<K extends keyof AdminProductForm>(
  errors: ProductFormErrors,
  field: K,
) {
  if (!errors[field]) {
    return errors
  }

  const nextErrors = { ...errors }
  delete nextErrors[field]
  return nextErrors
}

function selectedCategoryName(categories: Category[], categoryId: string) {
  return (
    categories.find((category) => category.id === Number(categoryId))?.name ??
    '카테고리'
  )
}

function readApiMessage(error: unknown, fallbackMessage: string) {
  if (error instanceof ApiError) {
    return error.message
  }

  return fallbackMessage
}
