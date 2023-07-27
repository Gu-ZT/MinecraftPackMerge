<template class="all">
  <div class="title">
    <el-image :src="main.icon" lazy style="width: 7vw;height: 7vw" :loading="main.loading"/>
    <el-text size="large" style="font-size: 4vw;color: white;">{{ main.name }}</el-text>
    <el-text size="large" style="font-size: 4vw;color: white;">在线构建</el-text>
  </div>
  <el-form style="margin: 10px">
    <el-row>
      <el-col :span="2"/>
      <el-col :span="10">
        <el-form-item label="游戏版本">
          <el-select v-model="downloadForm.version" collapse-tags style="width: 35vw;" collapse-tags-tooltip
                     :max-collapse-tags="4">
            <el-option v-for="(value) in mc_versions" :key="value.id" :value="value.value" :label="value.value"/>
            <div class="adds-item">

            </div>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="10">
        <el-form-item label="模组内容">
          <el-select v-model="downloadForm.mods" collapse-tags multiple style="width: 35vw;" collapse-tags-tooltip
                     :max-collapse-tags="4">
            <el-option v-for="(value) in mods" :key="value.id" :value="value.id" :label="value.name"/>
            <div class="adds-item">

            </div>
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="2"/>
      <el-form-item class="adds" label="附加模块">
        <el-table height="60vh" style="width: 70vw;" stripe border :data="adds" @selection-change="select">
          <el-table-column type="selection"/>
          <el-table-column label="ICON" :width="90">
            <template #default="{row}">
              <el-image style="width: 64px;height: 64px" lazy :src="row.icon"/>
            </template>
          </el-table-column>
          <el-table-column prop="name" :width="120" label="附加"/>
          <el-table-column prop="desc" label="简介"/>
        </el-table>
      </el-form-item>
    </el-row>
  </el-form>
  <el-row>
    <el-col :span="2"/>
    <div class="download">
      <el-button @click="download" type="success">
        <el-icon>
          <UploadFilled/>
        </el-icon>
        提交构建选项
      </el-button>
    </div>
  </el-row>
</template>

<script setup lang="ts">
import {Ref, ref} from "vue";
import {Request} from "@/request/Request";
import {UploadFilled} from '@element-plus/icons-vue';
import "element-plus/theme-chalk/dark/css-vars.css";

document.documentElement.className = 'dark';

let main: Ref<{ icon, name, desc, version, loading }> = ref({
  icon: "",
  name: "",
  desc: "",
  version: "",
  loading: true
});
let adds: Ref<{ icon, id, name, desc, weight, main, mc, required, default }[]> = ref([]);
let mods: Ref<{ id, name, desc, weight, main, mc, required, default }[]> = ref([]);
let mc_versions = ref([
  {id: 20, value: "1.20"},
  {id: 19, value: "1.19"}
]);
let downloadForm: Ref<{ version: string, adds: string[], mods: string[] }> = ref({
  version: "1.20",
  adds: [],
  mods: []
});

Request.get("/main", context => {
  main.value = context.data;
  main.value.icon = `${Request.baseURL}/icons/main`;
  main.value.loading = false;
});
Request.get("/adds", context => {
  adds.value = context.data;
  for (let v of adds.value) {
    v.icon = `${Request.baseURL}/icons/${v.id}`;
  }
});
Request.get("/mods", context => {
  mods.value = context.data;
});

function select(rows) {
  downloadForm.value.adds.length = 0;
  for (let row of rows) {
    downloadForm.value.adds.push(row.id);
  }
}

function download() {
  Request.post("download", downloadForm.value, context => {
    const url = window.URL.createObjectURL(new Blob([context]));
    let link = document.createElement('a');
    link.style.display = 'none';
    link.href = url;
    link.download = main.value.name + '+v' + main.value.version + '.zip';
    document.body.appendChild(link);
    link.click();
    URL.revokeObjectURL(link.href); // 释放URL 对象
    document.body.removeChild(link);
  }, {responseType: 'blob'}, false);
}
</script>

<style scoped lang="scss">
.title {
  border-style: solid;
  border-width: 0;
  border-bottom-width: 1px;
  border-bottom-color: white;
  height: 10vw;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
</style>
