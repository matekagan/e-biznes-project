
export const reshapeList = (list = [], colsNum = 4) => {
    if (list.length === 0) {
        return [];
    }
    const rowsNum = Math.ceil(list.length / colsNum);
    const rows = Array(rowsNum).fill().map(() => []);
    list.forEach((el, index) => rows[Math.floor(index / colsNum)].push(el));
    return rows;
};
