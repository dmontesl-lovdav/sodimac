import { TypedUseSelectorHook, useSelector } from "react-redux";
import { RootState } from "../localStore";

export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;