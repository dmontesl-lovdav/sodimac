$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
$excel.DisplayAlerts = $false
$workbook = $excel.Workbooks.Open('C:\workspace-fbc\backend\mrch.backend.somx.catalogos-api\src\main\resources\catalogos.xlsx')

Write-Host '=== HOJAS DISPONIBLES ==='
foreach ($sheet in $workbook.Sheets) {
    Write-Host $sheet.Name
}
Write-Host ''

foreach ($sheet in $workbook.Sheets) {
    Write-Host "=== HOJA: $($sheet.Name) ==="
    $usedRange = $sheet.UsedRange
    $rowCount = $usedRange.Rows.Count
    $colCount = $usedRange.Columns.Count
    Write-Host "Filas: $rowCount, Columnas: $colCount"

    # Headers
    $headers = @()
    for ($col = 1; $col -le $colCount; $col++) {
        $headers += $sheet.Cells.Item(1, $col).Text
    }
    Write-Host "Headers: $($headers -join ', ')"

    # First 10 rows
    Write-Host 'Primeras 10 filas de datos:'
    for ($row = 2; $row -le [Math]::Min(11, $rowCount); $row++) {
        $rowData = @()
        for ($col = 1; $col -le $colCount; $col++) {
            $rowData += $sheet.Cells.Item($row, $col).Text
        }
        Write-Host "  ${row}: $($rowData -join ' | ')"
    }
    Write-Host ''
}

$workbook.Close($false)
$excel.Quit()
[System.Runtime.Interopservices.Marshal]::ReleaseComObject($excel) | Out-Null
