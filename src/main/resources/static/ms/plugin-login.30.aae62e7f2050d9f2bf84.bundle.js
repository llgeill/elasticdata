(this.webpackJsonp=this.webpackJsonp||[]).push([[30],{122:function(e,t,i){"use strict";var n=this&&this.__awaiter||function(e,t,i,n){return new(i||(i=Promise))(function(s,r){function o(e){try{d(n.next(e))}catch(e){r(e)}}function a(e){try{d(n.throw(e))}catch(e){r(e)}}function d(e){e.done?s(e.value):new i(function(t){t(e.value)}).then(o,a)}d((n=n.apply(e,t||[])).next())})};Object.defineProperty(t,"__esModule",{value:!0});const s=i(1356),r=i(1357);i(1358);const o=i(25),a=i(1361),d=i(87),l=i(280),u=i(36),g=i(93),c=i(338);t.default=class{constructor(e,t){this._user="",this._layout=t,this._domBtns=o(r.default),this._domLogin=o(s.default),this._vmBtns=this.initBtnVm_(),this._vmLogin=this.initLoginVM_(),this.mountDom_(),u.UserSession.bindLoginPlugin(this)}showLoginDialog(){this.onPageLogin_(),this.mountDom_()}mountDom_(){u.UserSession.hasLogin||(o(this._layout.userZone).children().detach(),o(this._layout.userZone).append(this._domBtns))}initBtnVm_(){let e={login:this.onPageLogin_.bind(this),register:this.onPageRegister_.bind(this)};return a.applyBindings(e,this._domBtns[0]),e}initLoginVM_(){let e={user:a.observable(""),pwd:a.observable(""),login:this.onDgLogin_.bind(this),register:this.onDgRegister_.bind(this),findPwd:this.onDgFindPwd_.bind(this),close:this.onDgClose_.bind(this),keydown:this.onDgKeyDown_.bind(this)},t=window.location.search,i=g.UrlUtils.parseUrlParam(t);return i&&(i.reg&&this.onPageLogin_(),i.tel&&(e.user=a.observable(i.tel))),a.applyBindings(e,this._domLogin[0]),e}onPageLogin_(){this._dgViewHandle&&this._dgViewHandle.alive?this._dgViewHandle.show():(this._dgViewHandle=this._layout.dialogView.showView(this._domLogin,{alignOffset:{x:0,y:-100},border:!1,header:{title:"用户登录",iconClass:"lti-user"}}),this._dgViewHandle.autoClose=!1)}onPageRegister_(e,t){console.log("page register"),window.location.href="article.html?type=register"}onDgKeyDown_(e,t){return 13===t.keyCode&&(o(t.currentTarget).trigger("change"),setTimeout(()=>{this.onDgLogin_(e,t)},10)),!0}onDgLogin_(e,t){return n(this,void 0,void 0,function*(){if(console.log("on dialog login:",e.user(),e.pwd()),""!=e.user())if(""!=e.pwd())try{let t=yield l.UserRequest.login(e.user(),e.pwd());t&&(0==t.code?window.location.href=c.Header.getInstance().getCurrentPage()+".html":4001==t.code?d.LTNotify.error(`${t.message}(${e.user()})`):4002==t.code?(d.LTNotify.error(`${t.message}`),e.pwd("")):d.LTNotify.error("登录失败"))}catch(e){d.LTNotify.error(`${e&&e.message||e}`,"登录失败")}else d.LTNotify.error("请输入密码");else d.LTNotify.error("请输入用户名或手机号")})}onDgRegister_(e,t){window.location.href="article.html?type=register"}onDgFindPwd_(e,t){console.log("on dialog forget pwd"),window.location.href="article.html?type=forgetPwd"}onDgClose_(e,t){this._dgViewHandle&&this._dgViewHandle.alive&&this._dgViewHandle.close(),this._dgViewHandle=void 0}}},1356:function(e,t){t.default='<div class="login-panel">\r\n    <div class="body">\r\n        <form class="login-form" method="post" novalidate="novalidate">\r\n            <div class="form-group">\r\n                <div class="input-group">\r\n                    <div class="input-group-prepend">\r\n                        <div class="input-group-text"><i class="lti-user"></i></div>\r\n                    </div>\r\n                    <input id="login_name" class="form-control"\r\n                           data-bind="value:user,event:{keydown:keydown}"\r\n                           type="text" placeholder="用户名或手机号" name="name">\r\n                </div>\r\n            </div>\r\n            <div class="form-group">\r\n                <div class="input-group">\r\n                    <div class="input-group-prepend">\r\n                        <div class="input-group-text"><i class="lti-key"></i></div>\r\n                    </div>\r\n                    <input class="form-control" id="login_password"\r\n                           data-bind="value:pwd,event:{keydown:keydown}"\r\n                           type="password" placeholder="密码" name="password">\r\n                </div>\r\n            </div>\r\n        </form>\r\n        \x3c!--<h6 class="lt-text-danger" style="width: 300px;font-size: 0.8em">\r\n            由于新平台密码安全等级提高，如果您是原宝船网用户，原密码不能再使用，请您点击”忘记密码“来设置新的密码，给您带来不便敬请谅解\r\n        </h6>--\x3e\r\n        <div class="lt-flex align-items-center justify-content-between" style="padding-bottom: 10px">\r\n            <button class="lt-btn-empty empty-gray" data-bind="click:register">免费注册</button>\r\n            <button class="lt-btn-empty empty-gray" data-bind="click:findPwd">忘记密码?</button>\r\n        </div>\r\n        <div class="text-center">\r\n            <button class="btn btn-info btn-sm login-btn" data-bind="click:login">登录</button>\r\n        </div>\r\n    </div>\r\n</div>'},1357:function(e,t){t.default='<div class="login-buttons lt-flex align-items-center">\r\n\x3c!--    <a href="http://old.myships.com"><button class="lt-btn-empty">返回旧版本</button></a>--\x3e\r\n\x3c!--    <span>&nbsp;|</span>--\x3e\r\n    <button class="lt-btn-empty" data-bind="click:register"><i class="lti-fujianusermanage"></i>注册</button>\r\n    <span>|</span>\r\n    <button class="lt-btn-empty" data-bind="click:login"><i class="lti-user"></i>登录</button>\r\n</div>'},1358:function(e,t,i){var n=i(1359);"string"==typeof n&&(n=[[e.i,n,""]]),n.locals&&(e.exports=n.locals);(0,i(2).default)("6580e87c",n,!1,{})},1359:function(e,t,i){var n=i(1360);"string"==typeof n&&(n=[[e.i,n,""]]);var s={hmr:!0,transform:void 0,insertInto:void 0};i(1)(n,s);n.locals&&(e.exports=n.locals)},1360:function(e,t,i){(e.exports=i(0)(!1)).push([e.i,".login-buttons > button {\r\n    margin: auto 8px;\r\n}\r\n\r\n.login-buttons button {\r\n    color: #999;\r\n}\r\n\r\n.login-panel {\r\n    background: rgba(27, 26, 72, 0.75);\r\n    padding-top: 5px;\r\n}\r\n\r\n.login-panel > .body {\r\n    padding: 10px 20px;\r\n}\r\n\r\n.login-panel > .body .login-btn {\r\n    background:rgba(133,92,172,1);\r\n    width: 100%;\r\n}",""])},280:function(e,t,i){"use strict";var n=this&&this.__awaiter||function(e,t,i,n){return new(i||(i=Promise))(function(s,r){function o(e){try{d(n.next(e))}catch(e){r(e)}}function a(e){try{d(n.throw(e))}catch(e){r(e)}}function d(e){e.done?s(e.value):new i(function(t){t(e.value)}).then(o,a)}d((n=n.apply(e,t||[])).next())})};Object.defineProperty(t,"__esModule",{value:!0});const s=i(86),r=i(288),o=i(302);t.UserRequest=new class{constructor(){}ajax(e,t,i){return n(this,void 0,void 0,function*(){return s.AjaxUtil.ajax(e,t,i)})}login(e,t){return n(this,void 0,void 0,function*(){let i=(new Date).getTime();return yield this.ajax("/uc/sys/user/login","POST",{user:e,time:i,pwdSign:r(o(t)+i)})})}logout(){return n(this,void 0,void 0,function*(){return yield this.ajax("/uc/sys/user/logout","POST",void 0)})}verify(e){return n(this,void 0,void 0,function*(){return yield this.ajax("/uc/sys/user/verify","POST",e)})}smsCode(e){return n(this,void 0,void 0,function*(){return yield this.ajax("/uc/sys/user/smscode","POST",e)})}register(e){return n(this,void 0,void 0,function*(){return yield this.ajax("/uc/sys/user/register","POST",e)})}resetPassword(e){return n(this,void 0,void 0,function*(){return yield this.ajax("/uc/sys/user/resetPassword","POST",e)})}modifyPassword(e){return n(this,void 0,void 0,function*(){return yield this.ajax("/uc/sys/user/modifyPassword","POST",e)})}saveMail(e){return n(this,void 0,void 0,function*(){return yield this.ajax("/uc/sys/user/saveMail","POST",e)})}getMail(){return n(this,void 0,void 0,function*(){return yield this.ajax("/uc/sys/user/getMail","POST",void 0)})}markPoint(e){return n(this,void 0,void 0,function*(){let t=yield this.ajax("/ms/label/addPoint","POST",e);return 0==t.code?{flag:!0,message:t.message}:{flag:!1,message:t.message}})}updataPoint(e){return n(this,void 0,void 0,function*(){let t=yield this.ajax("/ms/label/updatePoint","POST",e);return 0==t.code?{flag:!0,message:t.message}:{flag:!1,message:t.message}})}delPoint(e){return n(this,void 0,void 0,function*(){let t=yield this.ajax("/ms/label/delPoint","POST",{id:e});return 0==t.code?{flag:!0,message:t.message}:{flag:!1,message:t.message}})}listPoint(){return n(this,void 0,void 0,function*(){let e=yield this.ajax("/ms/label/getPointGroup","POST",void 0);if(e.data.length>0)return e.data[0]})}addArea(e){return n(this,void 0,void 0,function*(){let t=yield this.ajax("/ms/label/addArea","POST",e);return 0==t.code?{flag:!0,message:t.message}:{flag:!1,message:t.message}})}addAreaGroup(e){return n(this,void 0,void 0,function*(){return yield this.ajax("/ms/label/addAreaGroup","POST",e)})}updateAreaGroup(e){return n(this,void 0,void 0,function*(){return yield this.ajax("/ms/label/updateAreaGroup","POST",e)})}deleteAreaGroup(e){return n(this,void 0,void 0,function*(){return yield this.ajax("/ms/label/delAreaGroup","POST",{id:e})})}deleteArea(e){return n(this,void 0,void 0,function*(){let t=yield this.ajax("/ms/label/delArea","POST",{id:e});return 0==t.code?{flag:!0,message:t.message}:{flag:!1,message:t.message}})}updateArea(e){return n(this,void 0,void 0,function*(){let t=yield this.ajax("/ms/label/updateArea","POST",e);return 0==t.code?{flag:!0,message:t.message}:{flag:!1,message:t.message}})}getAreaList(){return n(this,void 0,void 0,function*(){let e=yield this.ajax("/ms/label/getAreaGroup","POST",void 0);if(0==e.code)return e.data})}getAreaGroup(){return n(this,void 0,void 0,function*(){return yield this.ajax("/ms/label/getAreaGroup","POST",void 0)})}getMessage(){return n(this,void 0,void 0,function*(){let e=yield this.ajax("/ms/label/getMessage","POST",void 0);if(0==e.code)return e.data})}}},338:function(e,t,i){"use strict";Object.defineProperty(t,"__esModule",{value:!0});class n{constructor(){this.currentPage="index"}static getInstance(){return n.header}setCurrentPage(e){this.currentPage=e}getCurrentPage(){return this.currentPage}}n.header=new n,t.Header=n}}]);