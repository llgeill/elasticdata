function GetUrlParms() {
    for (var t = new Object, e = location.search.substring(1).split("&"), r = 0; r < e.length; r++) {
        var n = e[r].indexOf("=");
        if (-1 != n) {
            var o = e[r].substring(0, n), a = e[r].substring(n + 1);
            t[o] = unescape(a)
        }
    }
    return t
}

function loadJs(t) {
    var e = document.getElementById("loadScript"), r = document.getElementsByTagName("head").item(0);
    e && r.removeChild(e), script = document.createElement("script"), script.src = t, script.type = "text/javascript", script.id = "loadScript", r.appendChild(script)
}

function loadCss(t) {
    var e = document.getElementById("loadCss"), r = document.getElementsByTagName("head").item(0);
    e && r.removeChild(e), css = document.createElement("link"), css.href = t, css.rel = "stylesheet", css.type = "text/css", css.id = "loadCss", r.appendChild(css)
}

function formatDegree(t) {
    var e = 0 <= Number(t) ? "" : "-";
    t = Math.abs(t);
    var r = Math.floor(t);
    return e + " " + r + "-" + Math.floor(60 * (t - r)) + "-" + Math.round(3600 * (t - r) % 60)
}

function formatPointText(t) {
    var e = t.substring(t.indexOf("(") + 1, t.indexOf(" ")), r = t.substring(t.indexOf(" ") + 1, t.indexOf(")"));
    return formatDegree(e) + "E, " + formatDegree(r) + "N"
}

function null2Str(t) {
    for (var e in t) null == t[e] && (t[e] = "");
    return t
}

function stringToDate(t) {
    return t = t.replace(/-/g, "/"), new Date(t)
}

function sleep(t) {
    for (var e = Date.now(); Date.now() - e <= t;) ;
}

function debounce() {
    var t, e = arguments[0];
    if ("boolean" == typeof e) (t = arguments[1])._throttleID && clearTimeout(t._throttleID); else {
        t = e;
        var r = $.extend({context: null, args: [], time: 300}, arguments[1]);
        debounce(!0, t), t._throttleID = setTimeout(function () {
            t.apply(r.context, r.args)
        }, r.time)
    }
}

$.ajaxSetup({
    complete: function (t, e) {
        401 == t.status ? (layer && layer.closeAll("loading"), layer.msg("请登陆后使用！", {icon: 2}), WATHER.trigger("exit")) : 403 == t.status && (layer && layer.closeAll("loading"), layer.msg("对不起，您没有权限!", {icon: 2}))
    }
});
var login_stasus, JSTYPE = function () {
    var e = Object.prototype.toString;
    return {
        isString: function (t) {
            return "[object String]" === e.call(t)
        }, isNumber: function (t) {
            return "[object Number]" === e.call(t)
        }, isArray: function (t) {
            return "[object Array]" === e.call(t)
        }, isDate: function (t) {
            return "[object Date]" === e.call(t)
        }, isFunction: function (t) {
            return "[object Function]" === e.call(t)
        }, isBoolean: function (t) {
            return "[object Boolean]" === e.call(t)
        }, isUndefined: function (t) {
            return "[object Undefined]" === e.call(t)
        }, isObject: function (t) {
            return "[object Object]" === e.call(t)
        }, isBlank: function (t) {
            return "" === t || null === t
        }
    }
}(), WATHER = function () {
    var o = {};
    return {
        listen: function (t, e) {
            o[t] || (o[t] = []), o[t].push(e)
        }, trigger: function () {
            var t = Array.prototype.shift.call(arguments), e = o[t];
            if (!e || 0 === e.length) return !1;
            for (var r, n = 0; r = e[n++];) r.apply(this, arguments)
        }, remove: function (t, e) {
            var r = o[t];
            if (!r) return !1;
            if (e) for (var n = r.length - 1; 0 <= n; n--) {
                r[n] === e && r.splice(n, 1)
            } else r && (r.length = 0)
        }
    }
}();

function codeHandler(t, e, r) {
    200 == t.code ? e && e(t) : 400 == t.code ? (layer.msg("参数错误！", {icon: 2}), r && r(t)) : 401 == t.code ? (layer.msg("您尚未登录，请登录后使用！", {icon: 2}), r && r(t), WATHER.trigger("exit")) : (403 == t.code ? layer.msg("对不起，您没有该接口权限！", {icon: 2}) : 405 == t.code && layer.msg("服务接口关闭！", {icon: 2}), r && r(t))
}

function keysysmi(t, e) {
    var r;
    return 0 == t ? r = {limit: 8, offset: 0, wd: e} : 1 == t ? r = {
        left: e.left,
        top: e.top,
        right: e.right,
        bottom: e.bottom
    } : 2 == t ? r = {
        limit: e.limit,
        offset: e.offset + 1,
        wd: e.ftship
    } : 3 == t && (r = {mmsi: e}), maphaoEncrypt(JSON.stringify(r))
}

function maphaoEncryptJson(t) {
    return {param: maphaoEncrypt(JSON.stringify(t))}
}

function maphaoDecryptJson(t) {
    return JSON.parse(maphaoDecrypt(t))
}

function maphaoEncrypt(t) {
    var e = CryptoJS.enc.Utf8.parse(t), r = CryptoJS.enc.Utf8.parse("xhslcxjcmaphaoKY"), n = CryptoJS.enc.Utf8.parse("maphaoComToto_Iv");
    return CryptoJS.AES.encrypt(e, r, {iv: n, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7}).toString()
}

function maphaoDecrypt(t) {
    var e = CryptoJS.enc.Utf8.parse("xhslcxjcmaphaoKY"), r = CryptoJS.enc.Utf8.parse("maphaoComToto_Iv");
    return CryptoJS.AES.decrypt(t, e, {
        iv: r,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    }).toString(CryptoJS.enc.Utf8)
}