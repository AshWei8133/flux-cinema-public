export function toDataUrlFromBase64(raw) {
  if (!raw) return ''
  var s = String(raw).trim()
  if (s.indexOf('data:image/') === 0) return s
  var head = atob(s.slice(0, 12)).split('')
  var b0 = head.length > 0 ? head[0].charCodeAt(0) : 0
  var b1 = head.length > 1 ? head[1].charCodeAt(0) : 0
  var b2 = head.length > 2 ? head[2].charCodeAt(0) : 0
  var b3 = head.length > 3 ? head[3].charCodeAt(0) : 0
  var isPng = b0 === 0x89 && b1 === 0x50 && b2 === 0x4e && b3 === 0x47
  var isJpg = b0 === 0xff && b1 === 0xd8 && b2 === 0xff
  var mime = isPng ? 'image/png' : isJpg ? 'image/jpeg' : 'image/*'
  return 'data:' + mime + ';base64,' + s
}
