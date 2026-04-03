<template>
	<el-dialog :title="!form.id ? '新增' : '修改'" v-model="visible" width="600px">
		<el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
			<el-form-item label="预约编号" prop="yuyuebianhao">
				<el-input v-model="form.yuyuebianhao" placeholder="预约编号"></el-input>
			</el-form-item>
			<el-form-item label="通知类型" prop="tongzhileixing">
				<el-select v-model="form.tongzhileixing" placeholder="请选择通知类型">
					<el-option label="预约成功通知" :value="1"></el-option>
					<el-option label="就诊前一天提醒" :value="2"></el-option>
					<el-option label="就诊当天提醒" :value="3"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="通知类型名称" prop="tongzhileixingmingcheng">
				<el-input v-model="form.tongzhileixingmingcheng" placeholder="通知类型名称"></el-input>
			</el-form-item>
			<el-form-item label="用户账号" prop="zhanghao">
				<el-input v-model="form.zhanghao" placeholder="用户账号"></el-input>
			</el-form-item>
			<el-form-item label="医生账号" prop="yishengzhanghao">
				<el-input v-model="form.yishengzhanghao" placeholder="医生账号"></el-input>
			</el-form-item>
			<el-form-item label="通知内容" prop="tongzhineirong">
				<el-input type="textarea" v-model="form.tongzhineirong" placeholder="通知内容" :rows="4"></el-input>
			</el-form-item>
			<el-form-item label="发送状态" prop="fasongzhuangtai">
				<el-select v-model="form.fasongzhuangtai" placeholder="请选择发送状态">
					<el-option label="待发送" :value="0"></el-option>
					<el-option label="发送成功" :value="1"></el-option>
					<el-option label="发送失败" :value="2"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="发送时间" prop="fasongshijian">
				<el-date-picker v-model="form.fasongshijian" type="datetime" placeholder="选择发送时间"></el-date-picker>
			</el-form-item>
			<el-form-item label="重试次数" prop="chongshicishu">
				<el-input-number v-model="form.chongshicishu" :min="0" :max="10"></el-input-number>
			</el-form-item>
			<el-form-item label="最大重试次数" prop="zuidachongshicishu">
				<el-input-number v-model="form.zuidachongshicishu" :min="1" :max="10"></el-input-number>
			</el-form-item>
			<el-form-item label="失败原因" prop="shibaiyuanyin">
				<el-input type="textarea" v-model="form.shibaiyuanyin" placeholder="失败原因" :rows="2"></el-input>
			</el-form-item>
			<el-form-item label="接收状态" prop="jieshouzhuangtai">
				<el-select v-model="form.jieshouzhuangtai" placeholder="请选择接收状态">
					<el-option label="未接收" :value="0"></el-option>
					<el-option label="已接收" :value="1"></el-option>
					<el-option label="已读" :value="2"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="接收时间" prop="jieshoushijian">
				<el-date-picker v-model="form.jieshoushijian" type="datetime" placeholder="选择接收时间"></el-date-picker>
			</el-form-item>
			<el-form-item label="计划发送时间" prop="jihuafasongshijian">
				<el-date-picker v-model="form.jihuafasongshijian" type="datetime" placeholder="选择计划发送时间"></el-date-picker>
			</el-form-item>
		</el-form>
		<template #footer>
			<span class="dialog-footer">
				<el-button @click="visible = false">取 消</el-button>
				<el-button type="primary" @click="submitForm">确 定</el-button>
			</span>
		</template>
	</el-dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance } from 'vue'

const context = getCurrentInstance()?.appContext.config.globalProperties;
const emit = defineEmits(['formModelChange'])

const visible = ref(false)
const formRef = ref(null)
const form = reactive({
	id: null,
	yuyuebianhao: '',
	tongzhileixing: 1,
	tongzhileixingmingcheng: '',
	zhanghao: '',
	yishengzhanghao: '',
	tongzhineirong: '',
	fasongzhuangtai: 0,
	fasongshijian: null,
	chongshicishu: 0,
	zuidachongshicishu: 3,
	shibaiyuanyin: '',
	jieshouzhuangtai: 0,
	jieshoushijian: null,
	jihuafasongshijian: null
})

const rules = {
	yuyuebianhao: [{ required: true, message: '请输入预约编号', trigger: 'blur' }],
	tongzhileixing: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
	zhanghao: [{ required: true, message: '请输入用户账号', trigger: 'blur' }]
}

const init = (id = null, type = 'add') => {
	visible.value = true
	resetForm()
	if (id) {
		context.$http({
			url: `tongzhijilu/info/${id}`,
			method: 'get'
		}).then(res => {
			Object.assign(form, res.data.data)
		})
	}
}

const resetForm = () => {
	form.id = null
	form.yuyuebianhao = ''
	form.tongzhileixing = 1
	form.tongzhileixingmingcheng = ''
	form.zhanghao = ''
	form.yishengzhanghao = ''
	form.tongzhineirong = ''
	form.fasongzhuangtai = 0
	form.fasongshijian = null
	form.chongshicishu = 0
	form.zuidachongshicishu = 3
	form.shibaiyuanyin = ''
	form.jieshouzhuangtai = 0
	form.jieshoushijian = null
	form.jihuafasongshijian = null
}

const submitForm = () => {
	formRef.value.validate((valid) => {
		if (valid) {
			const url = form.id ? 'tongzhijilu/update' : 'tongzhijilu/save'
			const method = form.id ? 'post' : 'post'
			context.$http({
				url: url,
				method: method,
				data: form
			}).then(res => {
				context?.$toolUtil.message(form.id ? '修改成功' : '新增成功', 'success')
				visible.value = false
				emit('formModelChange')
			})
		}
	})
}

defineExpose({
	init
})
</script>

<style scoped>
.dialog-footer {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
}
</style>
