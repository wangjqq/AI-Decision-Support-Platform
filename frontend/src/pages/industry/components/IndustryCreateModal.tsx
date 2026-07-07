import { useEffect } from 'react'
import { Modal, Form, Input, Select, InputNumber, message } from 'antd'
import {
  useCreateIndustryMutation,
  useUpdateIndustryMutation,
  type IndustryCreateRequest,
  type IndustryUpdateRequest,
} from '../../../api/industryApi'
import { useAppDispatch, useAppSelector } from '../../../hooks/redux'
import { closeEditor } from '../../../stores/slices/industrySlice'

interface FormValues {
  code: string
  name: string
  level: number
  parentId?: number
  description?: string
  tags?: string
}

/** 行业层级（与后端保持一致） */
const LEVEL_OPTIONS = [
  { value: 1, label: '1 - 门类' },
  { value: 2, label: '2 - 大类' },
  { value: 3, label: '3 - 中类' },
  { value: 4, label: '4 - 小类' },
]

/**
 * 行业创建/编辑 Modal
 * - 复用同一个 Modal，根据 store.editorMode 切换标题
 * - code 仅创建时可编辑（与后端对齐：code 不可变）
 */
const IndustryCreateModal = () => {
  const [form] = Form.useForm<FormValues>()
  const dispatch = useAppDispatch()
  const { editorOpen, editorMode, editingIndustry } = useAppSelector((s) => s.industry)
  const [createIndustry, { isLoading: creating }] = useCreateIndustryMutation()
  const [updateIndustry, { isLoading: updating }] = useUpdateIndustryMutation()

  /** 打开时初始化表单 */
  useEffect(() => {
    if (!editorOpen) return
    if (editorMode === 'edit' && editingIndustry) {
      form.setFieldsValue({
        code: editingIndustry.code ?? '',
        name: editingIndustry.name,
        level: editingIndustry.level ?? 1,
        parentId: editingIndustry.parentId,
        description: editingIndustry.description,
        tags: editingIndustry.tags,
      })
    } else {
      form.resetFields()
      form.setFieldsValue({ level: 1 })
    }
  }, [editorOpen, editorMode, editingIndustry, form])

  const handleOk = async () => {
    try {
      const values = await form.validateFields()
      if (editorMode === 'edit' && editingIndustry) {
        const body: IndustryUpdateRequest = {
          name: values.name,
          level: values.level,
          parentId: values.parentId,
          description: values.description,
          tags: values.tags,
        }
        await updateIndustry({ id: editingIndustry.id, body }).unwrap()
        message.success('更新成功')
      } else {
        const body: IndustryCreateRequest = {
          code: values.code,
          name: values.name,
          level: values.level,
          parentId: values.parentId,
          description: values.description,
          tags: values.tags,
        }
        await createIndustry(body).unwrap()
        message.success('创建成功')
      }
      dispatch(closeEditor())
    } catch (e) {
      if (e && typeof e === 'object' && 'errorFields' in e) {
        return
      }
    }
  }

  return (
    <Modal
      title={editorMode === 'edit' ? '编辑行业' : '新增行业'}
      open={editorOpen}
      onCancel={() => dispatch(closeEditor())}
      onOk={handleOk}
      confirmLoading={creating || updating}
      okText={editorMode === 'edit' ? '保存' : '创建'}
      cancelText="取消"
      width={560}
      destroyOnClose>
      <Form<FormValues> form={form} layout="vertical" preserve={false}>
        <Form.Item
          label="行业编码"
          name="code"
          rules={[
            { required: true, message: '请输入行业编码' },
            { max: 32, message: '不超过 32 字符' },
          ]}>
          <Input
            placeholder="如：A01 / 011"
            disabled={editorMode === 'edit'}
            style={editorMode === 'edit' ? { color: '#94a3b8' } : undefined}
          />
        </Form.Item>
        <Form.Item
          label="行业名称"
          name="name"
          rules={[{ required: true, message: '请输入行业名称' }, { max: 100, message: '不超过 100 字' }]}>
          <Input placeholder="如：锂离子电池制造" />
        </Form.Item>
        <Form.Item
          label="行业层级"
          name="level"
          rules={[{ required: true, message: '请选择行业层级' }]}>
          <Select options={LEVEL_OPTIONS} placeholder="请选择层级" />
        </Form.Item>
        <Form.Item
          label="父行业 ID（可选）"
          name="parentId"
          tooltip="如二级 / 三级 / 四级行业需指定父级门类，填写父行业 ID；顶级门类留空"
          rules={[{ type: 'number', min: 1, message: '必须为正整数' }]}>
          <InputNumber placeholder="如：1" style={{ width: '100%' }} min={1} />
        </Form.Item>
        <Form.Item
          label="行业描述"
          name="description"
          rules={[{ max: 2000, message: '不超过 2000 字' }]}>
          <Input.TextArea rows={3} placeholder="行业简要描述（可选）" />
        </Form.Item>
        <Form.Item
          label="行业标签"
          name="tags"
          tooltip="多个标签以英文逗号分隔，如：新能源,锂电,储能"
          rules={[{ max: 200, message: '不超过 200 字符' }]}>
          <Input placeholder="如：新能源,锂电,储能" />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default IndustryCreateModal
