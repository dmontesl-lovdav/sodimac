import { FC, ButtonHTMLAttributes } from 'react';

type Variant = 'primary' | 'outline' | 'link' | 'outlineFill' | 'text' | 'cancel' | 'back';

interface GenericButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: Variant;
}

declare const GenericButton: FC<GenericButtonProps>;
export default GenericButton;
