export const ERR_INVALID_TYPE = 1;
export const ERR_INVALID_SIZE = 2;
export const ERR_TOO_LONG_FILENAME = 3;

export function getFileError(
  ef: File,
  validExts: string[],
  maxSize: number,
  maxNameLen: number
): number {
  const lower = ef.name.toLowerCase();
  const hasValidExt = validExts.some((ext) => lower.endsWith(`.${ext}`));
  if (!hasValidExt) return ERR_INVALID_TYPE;
  if (ef.size > maxSize) return ERR_INVALID_SIZE;
  if (ef.name.length > maxNameLen) return ERR_TOO_LONG_FILENAME;
  return 0;
}
