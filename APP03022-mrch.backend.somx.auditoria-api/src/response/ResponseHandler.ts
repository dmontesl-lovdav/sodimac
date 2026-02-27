
import { ResponseHandlerDTO } from "@/response/ResponseHandler.dto.js";
export class ResponseHandler {
    static responseBuilder(_message: string, _data: unknown, _errorCode: number, _httpStatus: number, _success: boolean, _detailError: unknown, _code = "")
                            : ResponseHandlerDTO {
        return {
                    data: _data,
                    message: _message,
                    errorCode: _errorCode,
                    code: _code,
                    httpStatus: _httpStatus,
                    success: _success,
                    detailError: _detailError,
                    timeStamp: Date.now()
        };
    }
}