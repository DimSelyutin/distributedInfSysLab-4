# -*- coding: utf-8 -*-
from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Pt, Cm
from docx.oxml.ns import qn

doc = Document()

for section in doc.sections:
    section.top_margin = Cm(2)
    section.bottom_margin = Cm(2)
    section.left_margin = Cm(3)
    section.right_margin = Cm(1.5)

style = doc.styles["Normal"]
style.font.name = "Times New Roman"
style.font.size = Pt(14)
style._element.rPr.rFonts.set(qn("w:eastAsia"), "Times New Roman")
style.paragraph_format.line_spacing = 1.15
style.paragraph_format.space_after = Pt(0)
style.paragraph_format.space_before = Pt(0)


def set_run_font(run, size=14, bold=False):
    run.font.name = "Times New Roman"
    run._element.rPr.rFonts.set(qn("w:eastAsia"), "Times New Roman")
    run.font.size = Pt(size)
    run.bold = bold


def add_center(text, bold=False, size=14):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(0)
    p.paragraph_format.space_before = Pt(0)
    run = p.add_run(text)
    set_run_font(run, size=size, bold=bold)
    return p


def add_left(text, bold=False, size=14, first_line=True):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.first_line_indent = Cm(1.25) if first_line else Cm(0)
    p.paragraph_format.space_after = Pt(6)
    run = p.add_run(text)
    set_run_font(run, size=size, bold=bold)
    return p


def add_heading_num(text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(12)
    run = p.add_run(text)
    set_run_font(run, size=14, bold=True)
    return p


def add_empty(n=1):
    for _ in range(n):
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(0)
        p.add_run("")


def set_cell_text(cell, text, bold=False, center=False):
    cell.text = ""
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER if center else WD_ALIGN_PARAGRAPH.LEFT
    run = p.add_run(text)
    set_run_font(run, size=12, bold=bold)


# ========== TITLE ==========
add_center("МИНИСТЕРСТВО ОБРАЗОВАНИЯ РЕСПУБЛИКИ БЕЛАРУСЬ", bold=True, size=12)
add_center("УЧРЕЖДЕНИЕ ОБРАЗОВАНИЯ", bold=True, size=12)
add_center("ГОМЕЛЬСКИЙ ГОСУДАРСТВЕННЫЙ ТЕХНИЧЕСКИЙ УНИВЕРСИТЕТ ИМЕНИ П. О. СУХОГО", bold=True, size=12)
add_empty(1)
add_center("Факультет автоматизированных и информационных систем", size=14)
add_center("Кафедра «Информационные технологии»", size=14)
add_center("дисциплина «Распределённые информационные системы»", size=14)
add_empty(2)
add_center("ОТЧЕТ ПО ЛАБОРАТОРНОЙ РАБОТЕ № 4", bold=True, size=14)
add_center("«Асинхронная многопоточная обработка изображений", bold=True, size=14)
add_center("в десктопном приложении»", bold=True, size=14)
add_empty(1)
add_center("Вариант 16", bold=True, size=14)
add_empty(4)

p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.RIGHT
run = p.add_run("Выполнил: студент гр. ЗИТ-31\nСелютин Д.И.\nПринял: преподаватель\n________________")
set_run_font(run, size=14)

add_empty(4)
add_center("Гомель 2026", size=14)

doc.add_page_break()

# ========== 1 ==========
add_heading_num("1 ЗАДАНИЕ")
add_left(
    "Цель работы: разработать десктопное приложение для асинхронной обработки "
    "изображения по пикселям, продемонстрировав асинхронный ввод-вывод файлов, "
    "пул потоков с очередью заданий, защиту критических секций, использование "
    "событий синхронизации, а также корректное завершение и отмену обработки "
    "без гонок и зависаний."
)
add_left(
    "В соответствии с вариантом 16 требуется реализовать обмен цветовых каналов "
    "R↔B с сохранением альфа-канала: для каждого пикселя ARGB формируется пиксель "
    "ABGR (красный и синий каналы меняются местами, зелёный канал и альфа остаются "
    "без изменений)."
)
add_left(
    "Приложение должно обеспечивать: выбор входного изображения; запуск обработки; "
    "индикатор прогресса; кнопку отмены; сохранение результата. Изображение "
    "разбивается на фрагменты (горизонтальные полосы), каждый фрагмент "
    "обрабатывается независимо, после чего фрагменты собираются в итоговое изображение."
)

# ========== 2 ==========
add_heading_num("2 СТРУКТУРА ПРИЛОЖЕНИЯ И СХЕМА КОНВЕЙЕРА")
add_left(
    "Программное обеспечение реализовано на языке Java 21 с использованием Maven, "
    "Lombok, Log4j2, JUnit 5 и Mockito. Архитектура разделена на слои: ui "
    "(графический интерфейс Swing), model (DTO), service (вся логика обработки)."
)
add_left(
    "Сервисный слой включает: ImageProcessingService (фасад для UI); пакет io "
    "(асинхронное чтение и запись изображения); пакет pipeline (планировщик тайлов, "
    "очередь заданий, пул рабочих потоков, сборщик результатов, учёт прогресса); "
    "пакет processing (фильтр обмена каналов R↔B на базе java.awt.Color)."
)
add_left("Схема конвейера обработки:")
add_left(
    "UI-поток → AsyncImageReader → TileScheduler → BlockingQueue → "
    "пул worker-потоков + Semaphore → ResultCollector / ProgressTracker → "
    "AsyncImageWriter → файл результата.",
    first_line=False,
)
add_left("Этапы конвейера:")
add_left("1) UI-поток управляет выбором файла, запуском, отображением прогресса и отменой;", first_line=False)
add_left("2) AsyncImageReader читает файл изображения асинхронно, не блокируя EDT;", first_line=False)
add_left("3) TileScheduler делит изображение на горизонтальные полосы заданной высоты;", first_line=False)
add_left("4) задания помещаются в BlockingQueue и обрабатываются фиксированным пулом потоков;", first_line=False)
add_left("5) ResultCollector под критической секцией записывает обработанные фрагменты в общий буфер;", first_line=False)
add_left("6) AsyncImageWriter асинхронно сохраняет итоговое изображение на диск.", first_line=False)

# ========== 3 ==========
add_heading_num("3 КРИТИЧЕСКИЕ СЕКЦИИ И СОБЫТИЯ СИНХРОНИЗАЦИИ")
add_left("3.1 Критические секции", bold=True, first_line=False)
add_left("Доступ к общим данным защищён явными критическими секциями (ключевое слово synchronized):")
add_left(
    "– ResultCollector.accept(...) — запись обработанного фрагмента в общий BufferedImage "
    "и журнал принятых тайлов;",
    first_line=False,
)
add_left(
    "– ProgressTracker.increment() — обновление счётчика завершённых фрагментов "
    "и расчёт процента прогресса;",
    first_line=False,
)
add_left(
    "Вне критических секций изменение общих структур (холст результата, счётчик прогресса) "
    "не допускается."
)
add_left("3.2 Механизмы синхронизации", bold=True, first_line=False)
add_left("В приложении использованы не менее двух различных механизмов синхронизации:")
add_left(
    "1) BlockingQueue (ArrayBlockingQueue) — общая очередь заданий; операция take() "
    "блокирует worker до появления задания (аналог сигнала «появилось задание»);",
    first_line=False,
)
add_left(
    "2) Semaphore — ограничивает число одновременно обрабатываемых фрагментов размером пула;",
    first_line=False,
)
add_left(
    "3) CountDownLatch — ожидание завершения всех фрагментов на этапе сборки конвейера;",
    first_line=False,
)
add_left(
    "4) AtomicBoolean cancelRequested — кооперативная отмена; при отмене пул останавливается "
    "(shutdownNow), незавершённые задания снимаются с latch, частичный результат не сохраняется.",
    first_line=False,
)
add_left(
    "Poison-pill объекты в очереди обеспечивают корректное завершение worker-потоков "
    "без зависаний."
)

# ========== 4 ==========
add_heading_num("4 РЕАЛИЗАЦИЯ ВАРИАНТА 16")
add_left(
    "Фильтр SwapRedBlueFilter для каждого пикселя фрагмента выполняет обмен каналов "
    "с помощью java.awt.Color:"
)
add_left("Color color = new Color(argb, true);", first_line=False)
add_left(
    "return new Color(color.getBlue(), color.getGreen(), color.getRed(), color.getAlpha()).getRGB();",
    first_line=False,
)
add_left(
    "Таким образом, красный и синий каналы меняются местами, зелёный канал и альфа-канал "
    "сохраняются. Обработка выполняется независимо для каждой горизонтальной полосы изображения."
)
add_left(
    "Корректность преобразования подтверждена модульными тестами JUnit 5 "
    "(PixelOpsTest, SwapRedBlueFilterTest), а работа конвейера — интеграционными тестами "
    "с Mockito (ImageProcessingServiceTest, ImageProcessingPipelineTest)."
)

# ========== 5 ==========
add_heading_num("5 РЕЗУЛЬТАТЫ ЗАМЕРОВ ВРЕМЕНИ")
add_left(
    "Замеры выполнены для изображения 1920×1080 (ARGB), высота тайла 32 пикселя "
    "(34 фрагмента). Для каждого размера пула выбиралось лучшее из трёх измерений. "
    "Результаты приведены в таблице 5.1."
)

p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
run = p.add_run("Таблица 5.1 – Время обработки при разном размере пула")
set_run_font(run, size=12, bold=False)

table = doc.add_table(rows=5, cols=6)
table.style = "Table Grid"
headers = ["Пул", "Чтение, мс", "Обработка, мс", "Запись, мс", "Всего, мс", "Тайлов"]
data = [
    ["1", "52", "76", "134", "267", "34"],
    ["2", "41", "53", "117", "217", "34"],
    ["4", "39", "53", "115", "213", "34"],
    ["8", "43", "64", "117", "229", "34"],
]
for i, h in enumerate(headers):
    set_cell_text(table.rows[0].cells[i], h, bold=True, center=True)
for r, row in enumerate(data, start=1):
    for c, val in enumerate(row):
        set_cell_text(table.rows[r].cells[c], val, center=True)

add_left(
    "При увеличении размера пула с 1 до 2–4 потоков время обработки фрагментов сокращается "
    "(76 мс → 53 мс). Дальнейшее увеличение до 8 потоков не даёт ускорения из‑за накладных "
    "расходов на переключение контекста и синхронизацию при относительно «лёгкой» операции "
    "обмена каналов. Существенную долю общего времени занимает асинхронная запись PNG "
    "(около 115–134 мс), которая слабо зависит от размера пула."
)

# ========== 6 ==========
add_heading_num("6 АНАЛИЗ ГОНОК И ВЗАИМНЫХ БЛОКИРОВОК")
add_left(
    "Общие данные (холст результата и счётчик прогресса) изменяются только внутри "
    "synchronized-методов, что исключает гонки записи. Очередь заданий потокобезопасна "
    "(BlockingQueue). Отмена реализована без взаимных блокировок: workers завершаются "
    "по poison-pill / interrupt, оставшиеся счётчики CountDownLatch освобождаются явно, "
    "файловые потоки закрываются в try-with-resources."
)
add_left(
    "При отмене обработки результат на диск не записывается, что соответствует требованию "
    "отсутствия частичного сохранения. Автотесты и практический прогон GUI не выявили "
    "зависаний потоков."
)

# ========== 7 ==========
add_heading_num("7 ЗАКЛЮЧЕНИЕ")
add_left(
    "1. Реализовано десктопное приложение для асинхронной многопоточной обработки "
    "изображений по варианту 16 (обмен каналов R↔B с сохранением альфы)."
)
add_left(
    "2. Конвейер включает асинхронный I/O, планировщик фрагментов, пул потоков с очередью, "
    "критические секции и несколько механизмов синхронизации "
    "(BlockingQueue, Semaphore, CountDownLatch)."
)
add_left(
    "3. Замеры показали ускорение этапа обработки при росте пула до 2–4 потоков; "
    "избыточный параллелизм (8 потоков) для данной лёгкой операции неэффективен."
)
add_left(
    "4. Отмена и завершение выполняются корректно: нет «зависших» потоков, незакрытых "
    "файлов и частичной записи результата."
)

out = r"e:\GitHub\gstu\labDistributedInfSyst\Отчет_ЛР4_Вариант16.docx"
doc.save(out)
print("saved", out)
