/**
 * src/utils/dateUtils.js
 * 通用的日期相關工具函數集合
 */

/**
 * 將 ISO 8601 格式的字串格式化為 "HH:mm" 的時間格式。
 *
 * @param {string | Date} isoString - ISO 8601 格式的日期時間字串或 Date 物件。
 * @returns {string} - 格式化後的 "HH:mm" 時間字串，若格式錯誤則返回 '無時間'。
 */
export const formatTime = (isoString) => {
  if (!isoString) return ''
  try {
    const date = new Date(isoString)
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  } catch (e) {
    console.error('時間格式化錯誤:', e)
    return '無時間'
  }
}

/**
 * 將日期物件格式化為 "YYYY年MM月DD日" 的中文日期格式。
 *
 * @param {Date} date - Date 物件。
 * @returns {string} - 格式化後的中文日期字串。
 */
export const getChineseDate = (date) => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}年${month}月${day}日`
}

/**
 * 判斷兩個日期物件是否是同一天。
 *
 * @param {Date} d1 - 第一個 Date 物件。
 * @param {Date} d2 - 第二個 Date 物件。
 * @returns {boolean} - 若為同一天則返回 true，否則返回 false。
 */
export const isSameDate = (d1, d2) => {
  return (
    d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate()
  )
}

/**
 * 判斷兩個日期物件是否是同一個月。
 *
 * @param {Date} d1 - 第一個 Date 物件。
 * @param {Date} d2 - 第二個 Date 物件。
 * @returns {boolean} - 若為同一個月則返回 true，否則返回 false。
 */
export const isSameMonth = (d1, d2) => {
  return d1.getFullYear() === d2.getFullYear() && d1.getMonth() === d2.getMonth()
}

/**
 * 根據電影院的「營業日」規則 (當天 06:00 至隔天 02:00)，
 * 將一個實際的 Date 物件，轉換成它所屬的「營業日」的 LocalDate 字串 (YYYY-MM-DD)。
 * * @param {Date} date - 實際的日期時間物件。
 * @returns {string} - 該時間所屬的營業日的 "YYYY-MM-DD" 字串。
 */
const getBusinessDateString = (date) => {
  let businessDate = new Date(date);
  // 如果時間在凌晨 00:00 到 05:59 之間，則此場次屬於前一天的營業日
  if (date.getHours() < 6) {
    businessDate.setDate(businessDate.getDate() - 1);
  }
  // 回傳標準格式的日期字串
  return formatDateString(businessDate);
};

/**
 * 將日期物件格式化為 "YYYY-MM-DD" 的字串。
 *
 * @param {Date} date - 日期物件。
 * @returns {string} - 格式化後的字串。
 */
export const formatDateString = (date) => {
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 將 Date 物件轉換為本地時區的 "YYYY-MM-DDTHH:mm:ss.sss" 格式字串 (不含 Z)。
 * 這個函式對於需要將前端本地時間傳送給後端，且後端期望接收無時區資訊的字串時非常有用。
 *
 * @param {Date} date - 要轉換的日期物件。
 * @returns {string} - 代表本地時間的類 ISO 格式字串。
 */
export const toLocalISOString = (date) => {
  const pad = (num) => (num < 10 ? '0' : '') + num;

  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  const seconds = pad(date.getSeconds());
  // .toFixed(3) 會回傳字串，.slice(2, 5) 取得小數點後三位
  const milliseconds = (date.getMilliseconds() / 1000).toFixed(3).slice(2, 5);

  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}.${milliseconds}`;
}

/**
 * 將日期時間字串或 Date 物件格式化為台灣常用的格式 (YYYY/MM/DD HH:mm)
 * @param {string | Date} dateTimeSource - 原始的日期時間字串或 Date 物件
 * @returns {string} 格式化後的日期時間字串，如果輸入無效則返回 ''
 */
export const formatDateTime = (dateTimeSource) => {
  if (!dateTimeSource) return '';

  const date = new Date(dateTimeSource);

  // 檢查日期是否有效
  if (isNaN(date.getTime())) {
    return '';
  }

  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false // 使用 24 小時制
  });
};

/**
 * 將 ISO 日期字串或 Date 物件格式化為訂票選單所需的格式。
 * 新增了對「今日」和「營業日」的判斷。
 * * @param {string | Date} dateSource - ISO 8601 格式的日期時間字串或 Date 物件。
 * @returns {{value: string, text: string} | null} - 返回格式化後的物件，若無效則返回 null。
 */
export const formatDateForBooking = (dateSource) => {
  if (!dateSource) return null;

  try {
    const sessionDate = new Date(dateSource);
    if (isNaN(sessionDate.getTime())) throw new Error("Invalid date source");

    // 1. 判斷此場次屬於哪個「營業日」
    const businessDateStr = getBusinessDateString(sessionDate); // "YYYY-MM-DD"

    // 2. 判斷今天的「營業日」是什麼
    const todayBusinessDateStr = getBusinessDateString(new Date());

    // 3. 組合顯示文字
    let displayText;
    const weekdays = ['日', '一', '二', '三', '四', '五', '六'];

    // 根據營業日來格式化日期，而不是實際日期
    const businessDateObj = new Date(businessDateStr + 'T00:00:00');
    const month = String(businessDateObj.getMonth() + 1).padStart(2, '0');
    const day = String(businessDateObj.getDate()).padStart(2, '0');
    const dayOfWeek = weekdays[businessDateObj.getDay()];

    if (businessDateStr === todayBusinessDateStr) {
      // 如果場次的營業日就是今天的營業日，顯示 "今日"
      displayText = `今日 ${month}/${day} (週${dayOfWeek})`;
    } else {
      // 否則，正常顯示日期
      displayText = `${month}/${day} (週${dayOfWeek})`;
    }

    return {
      value: businessDateStr,
      text: displayText
    };

  } catch (e) {
    console.error('訂票日期格式化錯誤:', e);
    return null;
  }
};

/**
 * 將 ISO 8601 格式的字串格式化為 "HH:mm" 的時間格式。
 * 新增了對「隔日」場次的標示。
 * * @param {string | Date} isoString - ISO 8601 格式的日期時間字串或 Date 物件。
 * @returns {string} - 格式化後的時間字串，例如 "10:30" 或 "01:30 (隔日)"。
 */
export const formatTimeForBooking = (isoString) => {
  if (!isoString) return '';
  try {
    const date = new Date(isoString);
    const hours = date.getHours();
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const hoursStr = hours.toString().padStart(2, '0');

    // 如果場次時間在凌晨 00:00 到 05:59 之間，標示為 (隔日)
    if (hours >= 0 && hours < 6) {
      return `${hoursStr}:${minutes} (隔日)`;
    }

    return `${hoursStr}:${minutes}`;
  } catch (e) {
    console.error('時間格式化錯誤:', e);
    return '無時間';
  }
};