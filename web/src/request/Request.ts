import axios from "axios";
import {frontConfig} from "@/config";
import {ElMessage} from "element-plus";

export class Request {
    public static baseURL: string = `${frontConfig.protocol}://${frontConfig.host}:${frontConfig.port}/api`; // "http://47.94.231.166:1001/api"
    public static instance = axios.create({
        baseURL: Request.baseURL,
        timeout: 15000
    })

    public static getBaseURL() {
        return Request.baseURL;
    }

    public static async post(link: string, data: object, callback: (response) => void, header: {} = this.defaultHandler(), verify: boolean = true) {
        await Request.instance.post(link, data, header)
            .then(res => Request.call(res.data, callback, verify))
            .catch(reason => Request.axiosError(reason))
    }

    public static async get(link: string, callback: (response) => void, verify: boolean = true) {
        await Request.instance.get(link, this.defaultHandler())
            .then(res => Request.call(res.data, callback, verify))
            .catch(reason => Request.axiosError(reason))
    }

    public static async put(link: string, data: object, callback: (response) => void, verify: boolean = true) {
        await Request.instance.put(link, data, this.defaultHandler())
            .then(res => Request.call(res.data, callback, verify))
            .catch(reason => Request.axiosError(reason))
    }

    public static async del(link: string, callback: (response) => void, verify: boolean = true) {
        await Request.instance.delete(link, this.defaultHandler())
            .then(res => Request.call(res.data, callback, verify))
            .catch(reason => Request.axiosError(reason))
    }

    private static async call(context, callback: (response) => void, verify: boolean) {
        if (verify) {
            if (context.code == 200) {
                callback(context);
            } else if (context.code == -2 || context.code == 401) {
            } else {
                Operate.error(context.msg);
            }
        } else {
            callback(context);
        }
    }

    public static async onSuccess(file, callback: (response) => void) {
        await Request.call(file, callback, true);
    }

    private static axiosError(reason) {
        Operate.error(reason.message);
    }

    public static defaultHandler(): { headers: { AuthToken: string } } {
        return {
            headers: {
                AuthToken: `${localStorage.getItem("token")}`
            }
        }
    }
}

export class Operate {
    public static error(msg): void {
        ElMessage.error(`操作失败：${msg}`);
    }

    public static success(): void {
        ElMessage.success('操作成功');
    }
}
