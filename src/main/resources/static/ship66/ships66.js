 function gzip(t) {
    //base64解密  切割成一个个字符   返回Unicode 编码的数组
    var e, i = atob(t).split("").map((function(t) {
            return t.charCodeAt(0)
        }
    )), n = new Uint8Array(i), r = pako.inflate(n), o = "";
    for (e = 0; e < r.length / 8192; e++)
        o += String.fromCharCode.apply(null, r.slice(8192 * e, 8192 * (e + 1)));
    return o += String.fromCharCode.apply(null, r.slice(8192 * e)),
        decodeURIComponent(escape(o))
}