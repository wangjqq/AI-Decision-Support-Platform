import { useEffect } from 'react'
import { Modal, Form, Input, DatePicker, Select, InputNumber, message } from 'antd'
import { useCreateCompanyMutation, useUpdateCompanyMutation, type CompanyCreateRequest, type CompanyUpdateRequest, type CompanyVO } from '../../../api/companyApi'
import { useAppDispatch, useAppSelector } from '../../../hooks/redux'
import { closeEditor } from '../../../stores/slices/companySlice'
import dayjs, { type Dayjs } from 'dayjs'

interface FormValues {
  name: string
  uscc: string
  industryId: number
  mainBusiness: string
  address?: string
  establishedAt?: Dayjs
  description?: string
}

/** 行业下拉（MVP 阶段硬编码 5 个，与后端 seed 对齐） */
const INDUSTRY_OPTIONS = [
  { value: 1, label: '锂离子电池' },
  { value: 2, label: '新能源汽车' },
  { value: 3, label: '光伏' },
  { value: 4, label: '人工智能' },
  { value: 5, label: '医疗器械' },
]

const CompanyCreateModal = () => {
  const [form] = Form.useForm<FormValues>()
  const dispatch = useAppDispatch()
  const { editorOpen, editorMode, editingCompany } = useAppSelector((s) => s.company)
  const [createCompany, { isLoading: creating }] = useCreateCompanyMutation()
  const [updateCompany, { isLoading: updating }] = useUpdateCompanyMutation()

  /** 打开时初始化表单 */
  useEffect(() => {
    if (!editorOpen) return
    if (editorMode === 'edit' && editingCompany) {
      form.setFieldsValue({
        name: editingCompany.name,
        uscc: editingCompany.uscc,
        industryId: editingCompany.industryId ?? 1,
        mainBusiness: editingCompany.mainBusiness,
        address: editingCompany.address,
        establishedAt: editingCompany.establishedAt
          ? dayjs(editingCompany.establishedAt)
          : undefined,
        description: editingCompany.description,
      })
    } else {
      form.resetFields()
      form.setFieldsValue({ industryId: 1 })
    }
  }, [editorOpen, editorMode, editingCompany, form])

  const handleOk = async () => {
    try {
      const values = await form.validateFields()
      const payload = {
        name: values.name,
        uscc: values.uscc,
        industryId: values.industryId,
        mainBusiness: values.mainBusiness,
        address: values.address,
        establishedAt: values.establishedAt?.format('YYYY-MM-DD'),
        description: values.description,
      }
      if (editorMode === 'edit' && editingCompany) {
        const body: CompanyUpdateRequest = payload
        await updateCompany({ id: editingCompany.id, body }).unwrap()
        message.success('更新成功')
      } else {
        const body: CompanyCreateRequest = payload
        await createCompany(body).unwrap()
        message.success('创建成功')
      }
      dispatch(closeEditor())
    } catch (e) {
      // 校验失败或业务错误（business error 已被 baseQuery 拦截并 toast）
      if (e && typeof e === 'object' && 'errorFields' in e) {
        // antd 校验错误，无需额外处理
        return
      }
    }
  }

  return (
    <Modal
      title={editorMode === 'edit' ? '编辑公司' : '新增公司'}
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
          label="公司名称"
          name="name"
          rules={[{ required: true, message: '请输入公司名称' }, { max: 100, message: '不超过 100 字' }]}>
          <Input placeholder="如：宁德时代新能源科技股份有限公司" />
        </Form.Item>
        <Form.Item
          label="统一社会信用代码"
          name="uscc"
          rules={[
            { required: true, message: '请输入 18 位统一社会信用代码' },
            { pattern: /^\d{18}$/, message: '必须为 18 位数字' },
          ]}>
          <Input placeholder="如：91350900161101155A" maxLength={18} />
        </Form.Item>
        <Form.Item
          label="所属行业"
          name="industryId"
          rules={[{ required: true, message: '请选择所属行业' }]}>
          <Select options={INDUSTRY_OPTIONS} placeholder="请选择" />
        </Form.Item>
        <Form.Item
          label="主营业务"
          name="mainBusiness"
          rules={[{ required: true, message: '请输入主营业务' }, { max: 500, message: '不超过 500 字' }]}>
          <Input.TextArea rows={2} placeholder="如：锂离子电池、储能系统及电池回收" />
        </Form.Item>
        <Form.Item label="注册地址" name="address" rules={[{ max: 200, message: '不超过 200 字' }]}>
          <Input placeholder="如：福建省宁德市蕉城区..." />
        </Form.Item>
        <Form.Item label="成立日期" name="establishedAt">
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" placeholder="选择日期" />
        </Form.Item>
        <Form.Item label="公司简介" name="description" rules={[{ max: 2000, message: '不超过 2000 字' }]}>
          <Input.TextArea rows={3} placeholder="公司简要介绍（可选）" />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default CompanyCreateModal
