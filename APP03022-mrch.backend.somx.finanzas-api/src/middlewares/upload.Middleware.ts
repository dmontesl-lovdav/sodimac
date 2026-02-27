import multer from 'multer';
import type { Request, Response, NextFunction } from "express";

// Configurar multer para usar memoria en lugar de disco
const storage = multer.memoryStorage();
const upload = multer({ storage: storage });
//const _upload = upload.array('files', 2);
//const upload = multer({ dest: os.tmpdir() });

// Promesa que envuelve la carga de Multer 
const multerPromise = (req: Request, res: Response) => { 
    const _upload = upload.array('files', res.locals.numberFiles);
    return new Promise<void>((resolve, reject) => { 
        _upload(req, res, (err: any) => { 
            
            if(!err) resolve(); 
            reject(err); 
        }); 
    }); 
};


export const uploadMiddleware = (numberOfFiles: number) => {
  return async (req: Request, res: Response, next: NextFunction) => { // Inner function is the actual middleware
    try {
       res.locals.numberFiles = numberOfFiles; 
       await multerPromise(req, res); 
       next(); 
    } catch(e) { 
       next(e); 
    } 
  };
};