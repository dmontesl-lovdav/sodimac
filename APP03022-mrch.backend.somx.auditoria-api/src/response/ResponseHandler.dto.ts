export interface ResponseHandlerDTO {
  data?: unknown;
  message: string;
  errorCode?: number;
  code?: string
  httpStatus: number; 
  success: boolean;
  detailError?: unknown;
  timeStamp: number;
}


export interface ResponsePageableDTO {
  content?: unknown;
  totalElements?: number;
  totalPages?: number;
  pageNumber?: number; 
  pageSize?: number;
  numberOfElements?: number;
}