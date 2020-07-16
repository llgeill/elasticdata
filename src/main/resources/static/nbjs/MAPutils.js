var MAPutils = {
    advsea: null,
    toppoint: null,
    tiandituDX: null,
    tiandituDXBZ: null,
    tiandituYX: null,
    tiandituYXBZ: null,
    haituwmts: null,
    GlobalMap: null,
    shipPathLineLayer: null,
    shipLayer: null,
    measureTooltipElementArr: new Array,
    helpTooltipElement: null,
    helpTooltip: null,
    measureTooltipElement: null,
    measureTooltip: null,
    popElement: [],
    shipGlobalStyle: [],
    screenText: {},
    heigthKey: 16,
    animType: {vhf: "VHFAnim", port: "portAnim", shipLine: "shipLineAnim", forbidInOut: "forbidInOutAnim"},
    naviMarkImagePre: "http://183.62.26.194:81",
    markLayerInd: {offset: null, pointAdd: null, pointImg: null, pointEdit: null},
    flashAnimStatus: {},
    allowRequestTime: 8e3,
    tempLayerObj: {},
    locatAnimPre: "LOCATANIM",
    locatAnimStatus: {},
    drawLineShipMmsi: !1,
    mouseInFeature: null,
    init: function () {
        MAPutils.toppoint = new ol.layer.Tile({
            source: new ol.source.TileWMS({
                url: "http://219.137.32.78:7001/maphaoserver/topp/wms",
                params: {
                    LAYERS: "topp:ls_table",
                    Width: 256,
                    Height: 256,
                    SRS: "EPSG:4326",
                    FORMAT: "image/png",
                    VERSION: "1.1.1"
                }
            })
        }), MAPutils.advsea = new ol.layer.Tile({source: new ol.source.XYZ({url: "map/getAdvSea?service=wmts&request=gettile&tilematrixset=advsea&tilematrix={z}&tilerow={y}&tilecol={x}&format=image/png&layer=default&style=default&version=1.0.0"})}), MAPutils.tiandituDX = new ol.layer.Tile({
            source: new ol.source.XYZ({
                url: "http://t{0-7}.tianditu.gov.cn/DataServer?T=vec_w&x={x}&y={y}&l={z}&tk=32b3d40bd8892a24cff87a51b19a820a",
                tileLoadFunction: function (e, t) {
                    e.getImage().src = t
                }
            })
        }), MAPutils.tiandituDXBZ = new ol.layer.Tile({source: new ol.source.XYZ({url: "http://t{0-7}.tianditu.gov.cn/DataServer?T=cva_w&X={x}&Y={y}&L={z}&tk=32b3d40bd8892a24cff87a51b19a820a"})}), MAPutils.tiandituYX = new ol.layer.Tile({source: new ol.source.XYZ({url: "http://t{0-7}.tianditu.gov.cn/DataServer?T=img_w&X={x}&Y={y}&L={z}&tk=32b3d40bd8892a24cff87a51b19a820a"})}), MAPutils.dixingtuOR = new ol.layer.Tile({source: new ol.source.XYZ({url: "http://t2.tianditu.gov.cn/DataServer?T=ter_w&X={x}&Y={y}&L={z}&tk=32b3d40bd8892a24cff87a51b19a820a"})}), MAPutils.dixingtuBZ = new ol.layer.Tile({source: new ol.source.XYZ({url: "http://t6.tianditu.gov.cn/DataServer?T=cta_w&X={x}&Y={y}&L={z}&tk=32b3d40bd8892a24cff87a51b19a820a"})}), MAPutils.dixingtuSJ = new ol.layer.Tile({source: new ol.source.XYZ({url: "http://t2.tianditu.gov.cn/DataServer?T=tbo_w&X={x}&Y={y}&L={z}&tk=32b3d40bd8892a24cff87a51b19a820a"})}), MAPutils.haituwmts = new ol.layer.Image({
            source: new ol.source.ImageArcGISRest({
                url: "map/MapServer",
                projection: "EPSG:3857"
            })
        }), MAPutils.xjseamap = new ol.layer.Tile({
            source: new ol.source.TileArcGISRest({
                url: "map/getXJSeaMap",
                projection: "EPSG:4326"
            })
        }), MAPutils.xjzhuantimap = new ol.layer.Tile({
            source: new ol.source.TileArcGISRest({
                url: "map/getXJZhuanTiMap",
                projection: "EPSG:4326"
            })
        }), MAPutils.initChangeMapMapping();
        var e = new ol.View({center: ol.proj.fromLonLat([113.6703, 22.6572]), maxZoom: 18, zoom: 9});
        MAPutils.GlobalMap = new ol.Map({
            controls: ol.control.defaults({
                zoom: !1,
                attribution: !1,
                rotate: !1
            }).extend([new ol.control.MousePosition({
                className: "ol-mouse-position-me", coordinateFormat: function (e) {
                    var t = ol.proj.toLonLat(e, "EPSG:3857"), i = MAPutils.ChangeToDFM(Math.abs(t[0])),
                        a = MAPutils.ChangeToDFM(Math.abs(t[1]));
                    return (0 < t[0] ? "东经" : "西经") + "：" + i + ("，" + (0 < t[1] ? "北纬" : "南纬") + "：") + a
                }
            })]),
            interactions: ol.interaction.defaults({altShiftDragRotate: !1, pinchRotate: !1, doubleClickZoom: !1}),
            layers: [MAPutils.advsea, MAPutils.tiandituDX, MAPutils.tiandituDXBZ, MAPutils.tiandituYX, MAPutils.dixingtuOR, MAPutils.dixingtuBZ, MAPutils.dixingtuSJ, MAPutils.haituwmts, MAPutils.xjseamap, MAPutils.xjzhuantimap, MAPutils.toppoint],
            target: "mapDiv",
            view: e
        }), MAPutils.initVector(), MAPutils.changeMap(1);
        for (var t = 0; t <= 6; t++) MAPutils.shipGlobalStyle[t] = MAPutils.getShipStyle(t);
        setInterval(function () {
            11 <= MAPutils.GlobalMap.getView().getZoom() && MAPutils.refreshShip()
        }, 6e3), MAPutils.GlobalMap.on("moveend", function (e) {
            var t = this.getView().getZoom();
            WGAuth.getUserName() && refreshGeometry && debounce(refreshGeometry.doRefresh, {args: [t]}), 11 <= t && debounce(MAPutils.refreshShip)
        }), MAPutils.GlobalMap.getView().on("change:resolution", function (e) {
            var t = this.getZoom(), i = MAPutils.getOverlayByType("portLayer");
            t < 11 ? (LayerControl.getCheckStatus("船舶点图层") && MAPutils.toppoint.setVisible(!0), MAPutils.shipLayer.getSource().clear(), MAPutils.portLayer.getVisible() && i.forEach(function (e) {
                e.setPosition(void 0)
            })) : (MAPutils.toppoint.setVisible(!1), MAPutils.portLayer.getVisible() && i.forEach(function (e) {
                e.setPosition(e.get("oldPosition"))
            }), debounce(MAPutils.refreshShip))
        }), MAPutils.GlobalMap.on("singleclick", function (e) {
            var t = MAPutils.GlobalMap.forEachFeatureAtPixel(e.pixel, function (e) {
                return e
            });
            t && null != t.get("layerType") && MAPutils.featureClick(t, e)
        }), MAPutils.GlobalMap.on("pointermove", function (e) {
            if (!e.dragging) {
                var t = MAPutils.GlobalMap.getEventPixel(e.originalEvent),
                    i = MAPutils.GlobalMap.forEachFeatureAtPixel(t, function (e) {
                        return e
                    });
                MAPutils.GlobalMap.hasFeatureAtPixel(t) ? (MAPutils.GlobalMap.getTargetElement().style.cursor = "pointer", MAPutils.mouseInFeature !== i && (MAPutils.mouseInFeature && MAPutils.mouseInFeature.dispatchEvent({
                    type: "mouseout",
                    event: e
                }), i.dispatchEvent({
                    type: "mousein",
                    event: e
                }), MAPutils.mouseInFeature = i)) : (MAPutils.mouseInFeature !== i && (MAPutils.mouseInFeature && MAPutils.mouseInFeature.dispatchEvent({
                    type: "mouseout",
                    event: e
                }), MAPutils.mouseInFeature = null), MAPutils.GlobalMap.getTargetElement().style.cursor = "")
            }
        })
    },
    ResolutionChangeEvt: {
        shipLine24hour: function (i) {
            var e = MAPutils.shipPathLineLayer;
            if (e.getVisible() && e.getSource().getFeatures().length) {
                var t = e.getSource();
                if (i) {
                    var a = new ol.format.WKT;
                    _.forEach(i, function (e) {
                        var t = a.readFeature(e.geom).getGeometry().getCoordinates();
                        i.longitude = t[0], i.latitude = t[1]
                    }), e.set("shipPoint", i)
                } else i = e.get("shipPoint");
                i && (i = MAPutils.DouglasPeuker(i), t.forEachFeature(function (e) {
                    "shipLine24_Point" == e.get("featureType") && t.removeFeature(e)
                }), _.forEach(i, function (e) {
                    MAPutils.drawShipPoint(e)
                }))
            }
        }
    },
    onChangeResolutionDebounce: function () {
        _.forEach(MAPutils.ResolutionChangeEvt, function (e, t) {
            e()
        })
    },
    initVector: function () {
        CONFIG.initLayer.forEach(function (e) {
            var t = e.alias || e.title, i = e.layerType, a = e.zIndex || 1, r = e.checked, o = e.retainFeature || !1;
            e.isParent || e.notCreate || (MAPutils[i] = new ol.layer.Vector({
                source: new ol.source.Vector,
                visible: r,
                title: t,
                retainFeature: o,
                layerType: i
            }), MAPutils[i].setZIndex(a), MAPutils.GlobalMap.addLayer(MAPutils[i]))
        }), MAPutils.toppoint.set("title", "船舶点图层"), MAPutils.initOverlay()
    },
    initOverlay: function () {
        var e = document.createElement("div");
        e.className = "css-animation", MAPutils.twinkleOverlay = new ol.Overlay({
            element: e,
            positioning: "center-center"
        }), MAPutils.GlobalMap.addOverlay(MAPutils.twinkleOverlay), MAPutils.createImageOverlay({
            url: "page/getAllSwz",
            src: "image/resource/swz.png",
            layerType: "swzLayer",
            kv: {authId: "WGA_button_hydro_station"}
        }), MAPutils.createImageOverlay({
            type: "navimark",
            url: "page/getAllSimpleNaviMark",
            layerType: "naviMarkLayer",
            drawToolTip: !1
        }), MAPutils.initAisMark(), MAPutils.initAllPort(), MAPutils.Popup.init()
    },
    locateAis: function (e) {
        MAPutils.createImgFeature({
            layer: MAPutils.useLayer("AIS基站"),
            layerType: "aisLayer",
            data: e,
            img: "image/resource/aisbs.png",
            text: e.name,
            cb: function () {
                MAPutils.locatWithTwinkle(MAPutils.getCenterById(MAPutils.aisLayer, e.id), 14, function () {
                    MAPutils.AISPop(e.id, MAPutils.getPopOffSet(), e)
                })
            }
        })
    },
    locateVhf: function (e) {
        MAPutils.createImgFeature({
            layer: MAPutils.useLayer("VHF基站"),
            layerType: "radioLayer",
            data: e,
            img: "image/resource/radio.png",
            text: e.names,
            cb: function () {
                MAPutils.locatTo(MAPutils.getCenterById(MAPutils.radioLayer, e.pid), 14, function () {
                    MAPutils.radioPop(e.pid, MAPutils.getPopOffSet(), e)
                })
            }
        })
    },
    createImgFeature: function (e) {
        e = $.extend({
            clear: !0,
            layer: null,
            layerType: null,
            data: null,
            img: null,
            scale: 1,
            text: null,
            cb: null
        }, e);
        var t = new ol.style.Style({image: new ol.style.Icon({src: e.img, scale: e.scale})});
        if (e.text) {
            var i = new ol.style.Text({
                font: "bold 12px Microsoft YaHei",
                text: e.text,
                fill: new ol.style.Fill({color: [67, 129, 209]}),
                stroke: new ol.style.Stroke({color: "#FFFFFF", width: 3}),
                textAlign: "center",
                textBaseline: "bottom",
                offsetY: -20
            });
            t.setText(i)
        }
        _.isFunction(e.clear) ? e.clear() : e.clear && e.layer.getSource().clear(), v = e.data, MAPutils.drawVectorFeature(e.layer, {
            geom: v.geom,
            style: t,
            id: v.pid || v.id,
            kv: {layerType: e.layerType, data: v}
        }), e.cb && e.cb()
    },
    initAisMark: function () {
        $.post("page/getAisMark", function (e) {
            if (_.isEmpty(e)) return console.warn("[page/getAisMark] 初始化ais航标（ais接入的航标，内存中）返回值为空,放弃初始化");
            _.forEach(e, function (e, t) {
                var i = 0 == e.isvirtual ? CONFIG.navmark["0306"] : CONFIG.navmark.virtual;
                MAPutils.drawVectorFeature(MAPutils.aisNaviMarkLayer, {
                    geom: e.geom,
                    style: new ol.style.Style({image: new ol.style.Icon({src: i.icon, scale: i.scale})}),
                    id: e.mmsi,
                    kv: {layerType: "aisNaviMarkLayer", data: e}
                })
            })
        })
    },
    initAllPort: function (e) {
        MAPutils.createImageOverlay({
            url: "page/getAllGkLocal",
            layerType: "portLayer",
            drawToolTip: !1,
            parseData: function (e) {
                var t = e.result || [];
                return t.length && (t = t.map(function (e) {
                    return e.pgeom = e.geom, e.geom = e.point, e
                })), t
            },
            styleFn: function (e) {
                return e
            }
        }), e && e()
    },
    Popup: function () {
        var a;
        return {
            init: function () {
                var e = document.getElementById("my-popup"),
                    t = (document.getElementById("my-popup-content"), document.getElementById("my-popup-closer"));
                t.onclick = function () {
                    return a.setPosition(void 0), t.blur(), !1
                }, a = new ol.Overlay({element: e}), MAPutils.GlobalMap.addOverlay(a);
                var i = new ol.Overlay({
                    id: "my_popup_1",
                    element: document.getElementById("typhoon-info-show"),
                    offset: [20, -80],
                    autoPan: !0
                });
                MAPutils.GlobalMap.addOverlay(i)
            }, open: function (e) {
                var t = $("#my-popup-content"), i = $.extend({coor: void 0, width: 300, height: 120, content: ""}, e);
                i.width && t.width(i.width), i.height && t.height(i.height), t.html(i.content), a.setPosition(i.coor)
            }, close: function () {
                a.setPosition(void 0), $("#my-popup-content").html("")
            }
        }
    }(),
    featureClick: function () {
        var o = {
            swzLayer: function (i) {
                $.post("html/swChart.html", function (e) {
                    layer.open({
                        id: "swChart",
                        type: 1,
                        title: i.get("names") || "水文站",
                        content: e,
                        shade: 0,
                        closeBtn: 1,
                        area: "1000px",
                        success: function (e, t) {
                            SWZModule.refreshEchart(i.getId(), i.get("names"))
                        }
                    })
                })
            }, hydroLayer: function (e, t) {
                var i = e.get("station"), a = e.get("time"), r = e.get("hydroMeteorData");
                MAPutils.hydroPop({station: i, time: a, hydroMeteorData: r}, t)
            }, radioLayer: function (e, t) {
                MAPutils.radioPop(e.getId(), t, e.get("data"))
            }, aisLayer: function (e, t) {
                MAPutils.AISPop(e.getId(), t, e.get("data"))
            }, naviMarkLayer: function (e, t) {
                MAPutils.naviMarkPop(e.getId(), t)
            }, aisNaviMarkLayer: function (e, t) {
                MAPutils.aisMarkPop(e.get("data"), t)
            }, weatherLayer: function (e, t) {
                MAPutils.weatherPop(e.getId(), t)
            }, shipLayer: function (e, t) {
                MAPutils.shipPop(e.getId(), t)
            }, navAnnunciateWarnLayer: function (e, t) {
                MAPutils.warningPop(e.getId(), t)
            }, p_navRuleLayer: function (e, t) {
                MAPutils.guidancePop(e, t)
            }, markPoint: function (e, t) {
                MAPutils.pointPop({feature: e, offset: t, title: "标注 - 兴趣点详情"})
            }, markArea: function (e, t) {
                MAPutils.pointPop({feature: e, offset: t, title: "标注 - 兴趣点详情"})
            }
        };
        return function (e, t) {
            var i = e.get("authId");
            if (!i || WGAuth.click("button", i)) {
                var a = e.get("layerType"), r = MAPutils.getLayerPixel(t);
                o[a] && o[a](e, r, t)
            }
        }
    }(),
    createLayerByName: function (e, t, i) {
        return MAPutils[e] = new ol.layer.Vector({
            source: new ol.source.Vector,
            visible: i
        }), JSTYPE.isNumber(t) && 0 < t && MAPutils[e].setZIndex(t), MAPutils.GlobalMap.addLayer(MAPutils[e]), MAPutils[e]
    },
    createLayerByTitle: function (e) {
        if (MAPutils.tempLayerObj[e]) return MAPutils.switchLayerByTitle(e, !0), MAPutils.tempLayerObj[e];
        var t = new ol.source.Vector, i = new ol.layer.Vector({title: e, source: t});
        return MAPutils.tempLayerObj[e] = i, MAPutils.GlobalMap.addLayer(i), i
    },
    useLayer: function (e) {
        var t = MAPutils.getLayerByTitle(e);
        return t ? (LayerControl.switch(e, !0), t) : null
    },
    clearLayer: function (e) {
        var t = this.getLayerByTitle(e);
        t && t.getSource().clear()
    },
    clearLayersByTitle: function (e) {
        var t = this;
        _.forEach(e, function (e) {
            t.clearLayer(e)
        })
    },
    createVectorLayer: function (e, t) {
        var i = MAPutils.getLayerByTitle(e);
        if (null == i && (i = new ol.layer.Vector({
            title: e,
            source: new ol.source.Vector
        }), MAPutils.GlobalMap.addLayer(i)), i.getSource().clear(), JSTYPE.isArray(t)) for (var a = 0; a < t.length; a++) MAPutils.drawVectorFeature(i, t[a]); else MAPutils.drawVectorFeature(i, t)
    },
    drawVectorFeature: function (e, t) {
        var i = (new ol.format.WKT).readGeometry(t.geom, {dataProjection: "EPSG:4326", featureProjection: "EPSG:3857"}),
            a = new ol.Feature({geometry: i});
        t.id && a.setId(t.id), t.style && a.setStyle(t.style);
        var r = t.kv;
        return r && Object.getOwnPropertyNames(r).forEach(function (e) {
            a.set(e, r[e])
        }), e.getSource().addFeature(a), a
    },
    getFeatureByParam: function (e) {
        var t = new ol.Feature({
            geometry: (new ol.format.WKT).readGeometry(e.geom, {
                dataProjection: "EPSG:4326",
                featureProjection: "EPSG:3857"
            })
        });
        e.id && t.setId(e.id), e.style && t.setStyle(e.style);
        var i = e.kv;
        return i && Object.getOwnPropertyNames(i).forEach(function (e) {
            t.set(e, i[e])
        }), t
    },
    drawShipPathLine: function (e, t) {
        var i = e.geom, a = MAPutils.drawVectorFeature(MAPutils.shipPathLineLayer, {
            geom: i,
            style: MAPutils.getArrowLineStyle({
                img: "image/common/arrow_blue.png",
                scale: .7,
                color: [6, 82, 221],
                width: 4,
                minPixel: 150
            })
        });
        t && t(a)
    },
    drawShipPoint: function (e) {
        MAPutils.drawVectorFeature(MAPutils.shipPathLineLayer, {
            geom: e.geom,
            kv: {data: e, featureType: "shipLine24_Point"},
            style: new ol.style.Style({
                image: new ol.style.RegularShape({
                    fill: new ol.style.Fill({color: "red"}),
                    stroke: new ol.style.Stroke({color: "red", width: 1}),
                    points: 100,
                    radius: 3,
                    angle: Math.PI / 4
                }),
                stroke: new ol.style.Stroke({color: "#FF0000", width: 1}),
                fill: new ol.style.Fill({color: "rgba(255,0,0,0.5)"}),
                text: new ol.style.Text({
                    font: "12px Microsoft YaHei",
                    text: MAPutils.formatTime(e.addtime) + "/" + e.sog + "节",
                    offsetY: -10,
                    fill: new ol.style.Fill({color: "red"})
                })
            })
        })
    },
    getLayerByTitle: function (e) {
        for (var t, i = MAPutils.GlobalMap.getLayers().getArray().slice().reverse(), a = 0; a < i.length; a++) if ((t = i[a]).get("title") == e) return t;
        return null
    },
    getFeatureByTitle: function (e, t) {
        for (var i, a = e.getSource().getFeatures().slice(0), r = 0, o = a.length; r < o; r++) if ((i = a[r]).get("title") == t) return i;
        return null
    },
    removeFeatureByTitle: function (t, e) {
        if (t && e) if (JSTYPE.isArray(e)) e.forEach(function (e) {
            MAPutils.removeFeatureByTitle(t, e)
        }); else {
            var i = MAPutils.getFeatureByTitle(t, e);
            i && t.getSource().removeFeature(i)
        }
    },
    formatTime: function (e) {
        return e.substring(0, e.lastIndexOf(":"))
    },
    measure: function () {
        if (null == MAPutils.isMeasureEnd || MAPutils.isMeasureEnd) {
            MAPutils.isMeasureEnd = !1;
            var i = +new Date, s = new ol.Sphere(6378137), e = new ol.source.Vector, a = new ol.source.Vector,
                t = new ol.layer.Vector({
                    source: e,
                    style: new ol.style.Style({
                        fill: new ol.style.Fill({color: "rgba(255, 0, 0, 0.9)"}),
                        stroke: new ol.style.Stroke({color: "rgba(255, 0, 0, 0.9)", width: 3})
                    })
                }), r = new ol.layer.Vector({
                    source: a,
                    style: new ol.style.Style({
                        fill: new ol.style.Fill({color: "rgba(255, 255, 255, 1)"}),
                        stroke: new ol.style.Stroke({color: "rgba(0, 0, 0, 0.5)", width: 2}),
                        image: new ol.style.Circle({
                            radius: 5,
                            stroke: new ol.style.Stroke({color: "rgba(255, 0, 0, 0.5)"}),
                            fill: new ol.style.Fill({color: "rgba(255, 255, 255, 1)"})
                        })
                    })
                });
            MAPutils.GlobalMap.addLayer(t), MAPutils.GlobalMap.addLayer(r);
            var o = document.createElement("a");
            o.href = "javascript:void(0);", o.classList.add("ol-popup-closer"), o.onclick = function (e) {
                MAPutils.GlobalMap.getOverlays().remove(MAPutils.GlobalMap.getOverlayById(i)), t.getSource().clear(), r.getSource().clear()
            }, function () {
                var t;
                MAPutils.drawMeasure = new ol.interaction.Draw({
                    source: e,
                    type: "LineString",
                    style: new ol.style.Style({
                        fill: new ol.style.Fill({color: "rgba(255, 255, 255, 0.2)"}),
                        stroke: new ol.style.Stroke({color: "rgba(0, 0, 0, 0.5)", width: 2}),
                        image: new ol.style.Circle({
                            radius: 5,
                            stroke: new ol.style.Stroke({color: "rgba(255, 0, 0, 0.5)"}),
                            fill: new ol.style.Fill({color: "rgba(255, 255, 255, 1)"})
                        })
                    })
                }), MAPutils.GlobalMap.addInteraction(MAPutils.drawMeasure), MAPutils.drawPointMeasure = new ol.interaction.Draw({
                    source: a,
                    type: "Point",
                    style: new ol.style.Style({
                        fill: new ol.style.Fill({color: "rgba(255, 255, 255, 1)"}),
                        stroke: new ol.style.Stroke({color: "rgba(255, 0, 0, 0.5)", width: 4}),
                        image: new ol.style.Circle({
                            radius: 5,
                            stroke: new ol.style.Stroke({color: "rgba(255, 0, 0, 0.5)"}),
                            fill: new ol.style.Fill({color: "rgba(255, 255, 255, 1)"})
                        })
                    })
                }), MAPutils.GlobalMap.addInteraction(MAPutils.drawPointMeasure), MAPutils.measureTooltipElement = document.createElement("div"), MAPutils.measureTooltipElement.className = "tooltip tooltip-measure", MAPutils.measureTooltip = new ol.Overlay({
                    id: i,
                    element: MAPutils.measureTooltipElement,
                    offset: [0, -15],
                    positioning: "bottom-center"
                }), MAPutils.GlobalMap.addOverlay(MAPutils.measureTooltip), MAPutils.helpTooltipElement && MAPutils.helpTooltipElement.parentNode.removeChild(MAPutils.helpTooltipElement), MAPutils.helpTooltipElement = document.createElement("div"), MAPutils.helpTooltipElement.className = "tooltip hidden", MAPutils.helpTooltip = new ol.Overlay({
                    element: MAPutils.helpTooltipElement,
                    offset: [15, 0],
                    positioning: "center-left"
                }), MAPutils.GlobalMap.addOverlay(MAPutils.helpTooltip), MAPutils.drawMeasure.on("drawstart", function (e) {
                    MAPutils.sketch = e.feature;
                    var a = e.coordinate;
                    t = MAPutils.sketch.getGeometry().on("change", function (e) {
                        var t, i = e.target;
                        i instanceof ol.geom.Polygon || i instanceof ol.geom.LineString && (t = function (e) {
                            var t, i = e.getCoordinates();
                            t = 0;
                            for (var a = MAPutils.GlobalMap.getView().getProjection(), r = 0, o = i.length - 1; r < o; ++r) {
                                var l = ol.proj.transform(i[r], a, "EPSG:4326"),
                                    n = ol.proj.transform(i[r + 1], a, "EPSG:4326");
                                t += s.haversineDistance(l, n)
                            }
                            return (Math.round(t / 1e3 * 100) / 100 * .5399568).toFixed(2) + " 海里"
                        }(i), a = i.getLastCoordinate()), MAPutils.measureTooltipElement.innerHTML = t, MAPutils.measureTooltipElementArr.push(MAPutils.measureTooltipElement), MAPutils.measureTooltip.setPosition(a)
                    })
                }, this), MAPutils.drawMeasure.on("drawend", function (e) {
                    MAPutils.isMeasureEnd = !0, MAPutils.measureTooltipElement.appendChild(o), MAPutils.measureTooltipElement.className = "tooltip tooltip-static", MAPutils.measureTooltip.setOffset([0, -7]), MAPutils.sketch = null, ol.Observable.unByKey(t), MAPutils.GlobalMap.removeInteraction(MAPutils.drawMeasure), MAPutils.GlobalMap.removeInteraction(MAPutils.drawPointMeasure)
                }, this)
            }()
        }
    },
    initChangeMapMapping: function () {
        var i = [].push, a = [];
        MAPutils.CHANGE_MAP_MAPPING = {
            0: MAPutils.advsea,
            1: [MAPutils.tiandituDX, MAPutils.tiandituDXBZ],
            2: MAPutils.tiandituYX,
            3: [MAPutils.tiandituDX, MAPutils.tiandituDXBZ, MAPutils.haituwmts],
            4: [MAPutils.dixingtuOR, MAPutils.dixingtuBZ, MAPutils.dixingtuSJ],
            5: [MAPutils.tiandituDX, MAPutils.tiandituDXBZ, MAPutils.xjseamap],
            6: [MAPutils.tiandituDX, MAPutils.tiandituDXBZ, MAPutils.xjzhuantimap]
        }, _.forEach(MAPutils.CHANGE_MAP_MAPPING, function (e, t) {
            _.isArray(e) ? i.apply(a, e) : a.push(e)
        }), MAPutils.MAP_ARRAY = _.uniq(a)
    },
    changeMap: function (e) {
        var t = this.CHANGE_MAP_MAPPING[e];
        if (_.isEmpty(t)) return console.error("切换的地图不存在，type:" + e);
        _.isArray(t) || (t = [t]), _.forEach(this.MAP_ARRAY, function (e) {
            e.setVisible(-1 != t.indexOf(e))
        })
    },
    moveTo: function (e, t, i) {
        t = t || 14;
        var a = MAPutils.GlobalMap.getView();
        a.setCenter(e), a.setZoom(t), i && MAPutils.twinkle(e)
    },
    locatFeature: function (e) {
        var t, i, a, r;
        (t = $.extend({
            layer: null,
            id: null,
            feature: null,
            extent: null,
            zoomSub: 1,
            cb: null,
            duration: 2e3
        }, e)).layer && MAPutils.switchLayer(t.layer, !0), t.extent || (i = t.feature ? t.feature : t.layer.getSource().getFeatureById(t.id), t.extent = i.getGeometry().getExtent()), r = MAPutils.calZoomByExtent(t.extent) - t.zoomSub, a = MAPutils.calExtentCenter(t.extent), MAPutils.flyTo({
            location: a,
            destZoom: r,
            duration: t.duration,
            done: function () {
                t.cb && t.cb({
                    feature: i,
                    viewCenter: MAPutils.GlobalMap.getView().getCenter(),
                    featureCenter: MAPutils.getFeatureCenter(i)
                })
            }
        })
    },
    calExtentCenter: function (e) {
        var t = e[0], i = e[1], a = e[2], r = e[3];
        return [t + Math.abs(Math.abs(a) - Math.abs(t)) / 2, i + Math.abs(Math.abs(r) - Math.abs(i)) / 2]
    },
    calZoomByExtent: function (e) {
        var t = MAPutils.GlobalMap.getView(), i = t.calculateExtent(), a = t.getZoom() || 18,
            r = (t.getResolution(), Math.abs(i[1] - i[3])), o = Math.abs(e[1] - e[3]);
        return Math.floor(Math.log(Math.pow(2, a) * r / o) / Math.log(2))
    },
    flyTo: function (e) {
        var t = $.extend({location: null, destZoom: 14, duration: 2e3, done: null}, e);
        14 < t.destZoom && (t.destZoom = 14);
        var i = MAPutils.GlobalMap.getView(), a = i.getZoom(),
            r = MAPutils.calTwoPointsDistance(t.location, i.getCenter()), o = i.getResolution(), l = Math.ceil(r / o);
        800 <= l && l <= 1500 ? a = 10 : 1500 < l && (a = 9), l < 1e3 && (t.duration = Math.ceil(t.duration * l / 1e3));
        var n = 2, s = !1;

        function u(e) {
            --n, s || 0 !== n && e || (s = !0, t.done && t.done(e))
        }

        t.location ? i.animate({center: t.location, duration: t.duration}, u) : --n, 1e3 < l ? i.animate({
            zoom: a,
            duration: t.duration / 2
        }, {zoom: t.destZoom, duration: t.duration / 2}, u) : i.animate({zoom: t.destZoom, duration: t.duration}, u)
    },
    locatTo: function (e, t, i) {
        t = t || 14;
        layer.close(MAPutils.DialogIndex), MAPutils.flyTo({location: e, destZoom: t, done: i})
    },
    locatWithTwinkle: function (e, t, i) {
        MAPutils.locatTo(e, t, function () {
            MAPutils.twinkle(e, 3e3), i && i()
        })
    },
    twinkle: function (e, t) {
        MAPutils.twinkleOverlay.setPosition(e), MAPutils.twinkleTimer && clearTimeout(MAPutils.twinkleTimer), MAPutils.twinkleTimer = setTimeout(function () {
            MAPutils.twinkleOverlay.setPosition(void 0)
        }, t || 3e3)
    },
    createNameTooltip: function (e) {
        var t = document.createElement("div");
        t.innerHTML = e.content, t.className = e && e.className || "tooltip";
        var i = new ol.Overlay({
            element: t,
            offset: e && e.offset || [0, -15],
            position: e.position,
            positioning: e && e.positioning || "bottom-center"
        });
        i.set("oldPosition", e.position);
        var a = e.kv;
        return a && Object.getOwnPropertyNames(a).forEach(function (e) {
            i.set(e, a[e])
        }), MAPutils.GlobalMap.addOverlay(i), t
    },
    createImageOverlay: function (u) {
        $.post(u.url, u.param, function (e) {
            var t, i, r = !(null != u.drawToolTip && !u.drawToolTip);
            if (t = u.parseData ? u.parseData(e) : e.result) {
                var o = u.layerType, n = MAPutils[o], s = u.kv || {};
                n.getSource().clear(), "navimark" == u.type || i || u.styleFn || (i = new ol.style.Style({image: new ol.style.Icon({src: u.src})})), $.each(t, function (e, l) {
                    if ("0306" == l.typecode) return !0;
                    if ("navimark" == u.type) {
                        var a = CONFIG.navmark[l.typecode];
                        a = a ? a.hasFunction ? a[l.functioncode] ? a[l.functioncode] : CONFIG.navmark.other : a : CONFIG.navmark.other, i = function (e, t) {
                            var i = MAPutils.GlobalMap.getView().getZoom() >= CONFIG.navmark.minZoomToShow ? a : CONFIG.navmark.other;
                            return new ol.style.Style({image: new ol.style.Icon({src: i.icon, scale: i.scale})})
                        }
                    }
                    "portLayer" == u.layerType && (u.styleFn = null, i = function (e, t) {
                        var i = MAPutils.GlobalMap.getView().getZoom(), a = CONFIG.gktype[l.type || "0"],
                            r = i >= CONFIG.navmark.minZoomToShow ? a[0] : a[1],
                            o = i >= CONFIG.navmark.minZoomToShow ? l.names : "";
                        return new ol.style.Style({
                            image: new ol.style.Icon({src: r.icon, scale: r.scale}),
                            text: new ol.style.Text({
                                font: "bold 12px Microsoft YaHei",
                                text: o,
                                fill: new ol.style.Fill({color: [67, 129, 209]}),
                                stroke: new ol.style.Stroke({color: "#FFFFFF", width: 3}),
                                textAlign: "center",
                                textBaseline: "bottom",
                                offsetY: -20
                            })
                        })
                    });
                    var t = MAPutils.drawVectorFeature(n, {
                        geom: l.geom,
                        style: u.styleFn && u.styleFn(l) || i,
                        id: l.pid || l.id,
                        kv: $.extend(s, {layerType: o, names: l.names, data: l})
                    });
                    r && MAPutils.createNameTooltip({
                        content: l.names,
                        position: MAPutils.getFeatureCenter(t),
                        kv: {layerType: o}
                    }), u.singleDataCb && u.singleDataCb(l, t)
                })
            }
        })
    },
    createWeatherOverlay: function () {
        $.post("page/getAllWeather", function (e) {
            if (e) {
                MAPutils.weatherLayer.getSource().clear();
                var a = new ol.style.Style({
                    image: new ol.style.RegularShape({
                        points: 4,
                        radius: 28,
                        rotation: .7853982,
                        fill: new ol.style.Fill({color: [123, 123, 123, .7]})
                    })
                });
                $.each(e, function (e, t) {
                    var i = new ol.style.Style({image: new ol.style.Icon({src: t.sk_i})});
                    MAPutils.drawVectorFeature(MAPutils.weatherLayer, {
                        geom: t.geom,
                        style: [a, i],
                        id: t.pid,
                        kv: {layerType: "weatherLayer", data: t}
                    })
                })
            }
        })
    },
    createFeatureOverlay: function (e) {
        var t = e.pid, i = e.layerType, a = MAPutils[i], r = a.getSource().getFeatureById(t);
        if (r) return r;
        r = MAPutils.drawVectorFeature(a, {geom: e.geom, id: t, kv: {layerType: i, names: e.names}});
        var o = MAPutils.getFeatureCenter(r);
        return MAPutils.createNameTooltip({
            content: e.names,
            position: o,
            offset: [0, 0],
            positioning: "center-center",
            kv: {layerType: i}
        }), r
    },
    ForbidInOut: function () {
        var o, l, a, i, r, n;
        o = {1: "新疆禁止进入区域", 2: "新疆禁止离开区域"}, l = {1: [255, 204, 51, 1], 2: [255, 64, 64, 1]}, r = function (e) {
            var t = $.extend({text: null, type: 1}, e);
            return 1 == t.type ? new ol.style.Style({
                fill: new ol.style.Fill({color: [255, 204, 51, .2]}),
                stroke: new ol.style.Stroke({color: "#ffcc33", width: 2}),
                image: new ol.style.Circle({radius: 7, fill: new ol.style.Fill({color: "#ffcc33"})}),
                text: new ol.style.Text({font: "12px Microsoft YaHei", text: t.text})
            }) : 2 == t.type ? new ol.style.Style({
                fill: new ol.style.Fill({color: [255, 64, 64, .2]}),
                stroke: new ol.style.Stroke({color: "#FF4040", width: 2}),
                image: new ol.style.Circle({radius: 7, fill: new ol.style.Fill({color: "#FF4040"})}),
                text: new ol.style.Text({font: "12px Microsoft YaHei", text: t.text})
            }) : void 0
        }, a = function (t) {
            $.ajax({
                url: "xj/call",
                type: "post",
                dataType: "json",
                data: {type: t.type, jroute: "remind/listAllArea", username: WGAuth.getUserName()},
                success: function (e) {
                    e.result && e.result.length ? e.success ? (i({
                        arr: e.result,
                        type: t.type
                    }), t.cb && t.cb()) : layer.msg("请求数据异常，请稍后再试！") : MAPutils.useLayer(o[t.type]).getSource().clear()
                }
            })
        }, i = function (t) {
            var i = MAPutils.useLayer(o[t.type]);
            i.getSource().clear(), t.arr.forEach(function (e) {
                MAPutils.drawVectorFeature(i, {
                    geom: e.geom,
                    id: e.pid,
                    style: r({type: t.type, text: e.names}),
                    kv: {data: e}
                })
            })
        }, n = function (e, t) {
            return MAPutils.useLayer(o[t]).getSource().getFeatureById(e)
        };
        var s = function (e, t, i) {
            var a = e.get("data"), r = MAPutils.useLayer(o[t]);
            MAPutils.locatFeature({
                layer: r, feature: e, cb: function () {
                    MAPutils.startPolygonAnim({layer: r, geom: a.geom, color: l[t]}), i && i()
                }
            })
        };
        return {
            refresh: function (e, t) {
                e = e || [1, 2], JSTYPE.isArray(e) && e.forEach(function (e) {
                    a({type: e, cb: t})
                }), JSTYPE.isNumber(e) && a({type: e, cb: t})
            }, locat: function (e) {
                var t = (e = e || {}).type, i = n(e.id, t);
                i ? s(i, t, e.cb) : a({
                    type: t, cb: function () {
                        i = n(e.id, t), s(i, t, e.cb)
                    }
                })
            }
        }
    }(),
    drawLineByMmsi: function (e, s) {
        if (WGAuth.click("button", "WGA_button_24hour_guiji")) {
            s = s || {};
            var t = $.extend({mmsi: e}, s.requestParam);
            s = $.extend({
                pointUrl: "ship/get24shipPoint",
                lineUrl: "ship/get24shipLine",
                title: "24小时轨迹",
                name: !1,
                callback: !1
            }, s), layer.load(), $.when($.post(s.pointUrl, t), $.post(s.lineUrl, t)).done(function (e, t) {
                var i, a;
                layer.closeAll("loading");
                try {
                    if (i = e[0].data, a = t[0].data, !(_.isArray(i) && _.isArray(a) && i.length && a.length)) throw new Error("drawLineByMmsi, 返回值 不是数组或数组为空")
                } catch (e) {
                    return layer.msg("暂无轨迹信息!", {icon: 5, anim: 6}), void console.error(e)
                }
                MAPutils.shipPathLineLayer.getSource().clear(), MAPutils.shipPathLineLayer.setVisible(!0);
                for (var r = a.length, o = [], l = 0; l < r; l++) MAPutils.drawShipPathLine(a[l], function (e) {
                    o.push(e.getGeometry().getExtent())
                });
                for (l = 0; l < i.length; l++) MAPutils.drawShipPoint(i[l]);
                LayerControl.switch("船舶轨迹", !0);
                var n = MAPutils.WKT.readGeometry(i[i.length - 1].geom).getCoordinates();
                MAPutils.moveTo(n, 13, !1), layer.closeAll("loading"), s.callback && s.callback()
            }).fail(function () {
                layer.closeAll("loading"), layer.msg("网络异常，请稍后再试!", {icon: 5, anim: 6})
            })
        }
    },
    refreshShip: function (e) {
        MAPutils.getShips(e)
    },
    getShips: function (r) {
        var e = MAPutils.getViewExtend(),
            t = maphaoEncryptJson({left: e.left, top: e.top, right: e.right, bottom: e.bottom});
        $.ajax({
            type: "POST", url: "ship/getShip", data: t, dataType: "json", success: function (e) {
                if (e.result && (e.result = maphaoDecryptJson(e.result), MAPutils.shipLayer.getSource().clear(), MAPutils.clearScreenText(), !(MAPutils.GlobalMap.getView().getZoom() < 11) && e)) {
                    var t = e.result;
                    if (t && "false" != t) {
                        t.length <= 50 ? (MAPutils.isShowShipName = !0, MAPutils.isShowShipShape = !0) : (MAPutils.isShowShipName = !1, MAPutils.isShowShipShape = !1);
                        for (var i = 0; i < t.length; i++) MAPutils.parseShipData(t[i]);
                        if (MAPutils.GlobalMap.getView().getZoom() < 11) return void MAPutils.shipLayer.getSource().clear();
                        WATHER.trigger("refreshShip"), r && r();
                        var a = MAPutils.GlobalMap.getView().getZoom();
                        refreshGeometry && refreshGeometry.doRefreshAdjacentRoute(!1, a)
                    }
                }
            }
        })
    },
    parseShipData: function (e) {
        var t = parseFloat(e.thg);
        if (t < 0 || 360 < t) {
            var i = parseFloat(e.cog);
            t = 0 <= i && i <= 360 ? i : 0
        }
        var a = e.sog;
        0 < a.length && (a = a.substring(0, 6)), $.extend(e, {
            rote: t,
            speed: a,
            layerType: e.layerType || "shipLayer",
            isShowShipShape: e.isShowShipShape ? "yes" == e.isShowShipShape : MAPutils.isShowShipShape,
            isShowName: e.isShowShipName ? "yes" == e.isShowShipName : MAPutils.isShowShipName,
            shipLayer: e.shipLayer || MAPutils.shipLayer
        }), MAPutils.drawShips(e)
    },
    drawShips: function (e) {
        var t = e.shipLayer, i = (new ol.format.WKT).readGeometry(e.geom, {
            dataProjection: "EPSG:4326",
            featureProjection: "EPSG:3857"
        }).getCoordinates(), a = new Array;
        102.3 <= e.speed && (e.speed = 0), a[0] = MAPutils._isShowShipShape(e.length) ? MAPutils.getShipShape(i, e) : MAPutils.getTrian(i, e);
        var r = -Math.PI / 180 * parseFloat(e.rote), o = new ol.geom.Polygon(a);
        o.rotate(r, i);
        var l = new ol.Feature({geometry: o});
        l.setId(e.mmsi), l.set("geom", i), l.set("name", e.names), l.set("mmsi", e.mmsi), l.set("type", e.type), l.set("thg", e.rote), l.set("layerType", e.layerType), l.set("sog", e.speed), l.set("length", e.length), l.set("width", e.width), l.set("isfleets", e.isfleets), l.set("data", e), l.setStyle(MAPutils.getShipStyleByCache(e.type)), e.isShowName = MAPutils.GlobalMap.getView().getZoom() >= CONFIG.zoomShowShipLabel, e.isShowName && MAPutils.drawVectorFeature(t, {
            id: "label" + e.mmsi,
            geom: e.geom,
            style: MAPutils.getShipStyleTest(e)
        }), t.getSource().addFeature(l)
    },
    _isShowShipShape: function (e) {
        return 20 < (e || 0) / MAPutils.GlobalMap.getView().getResolution()
    },
    drawRect: function (e, t) {
        var i = e[0], a = e[1], r = MAPutils.getResolution(), o = r[0], l = r[1], n = [], s = [i - 10 * o, a - 10 * l],
            u = [i + 10 * o, a - 10 * l], c = [i + 10 * o, a + 10 * l], d = [i - 10 * o, a + 10 * l],
            y = [i - 10 * o, a - 10 * l];
        n.push(s), n.push(u), n.push(c), n.push(d), n.push(y);
        var p = new Array;
        p[0] = n;
        var g = new ol.geom.Polygon(p), f = new ol.Feature({geometry: g});
        return f.set("geom", e), f.set("name", t.names), f.set("mmsi", t.mmsi), f.set("type", t.type), f.set("thg", t.rote), f.set("layerType", "ship"), f.set("sog", t.speed), f.set("length", t.length), f.set("width", t.width), f.set("isfleets", t.isfleets), f.setStyle(MAPutils.transStyle), f
    },
    getTrian: function (e, t) {
        var i = e[0], a = e[1], r = MAPutils.getResolution(), o = r[0], l = r[1], n = [], s = [i - 6 * o, a - 10 * l],
            u = [i + 6 * o, a - 10 * l], c = [i, a + 10 * l], d = [i, a + 10 * l + MAPutils.getSpeedLine(t.speed)],
            y = [i, a + 10 * l], p = [i - 6 * o, a - 10 * l];
        return n.push(s), n.push(u), n.push(c), n.push(d), n.push(y), n.push(p), n
    },
    getShipShape: function (e, t) {
        var i = e[0], a = e[1], r = (MAPutils.getResolution()[1], []), o = MAPutils.getSpeedLine(t.speed), l = 4,
            n = 12;
        0 < t.width && (l = t.width / 2), 0 < t.length && (n = t.length / 2);
        var s = l < n ? l : n, u = [i - l, a - n], c = [i + l, a - n], d = [i + l, a + n - s], y = [i, a + n],
            p = [i, a + n + o], g = [i, a + n], f = [i - l, a + n - s], m = [i - l, a - n];
        return r.push(u), r.push(c), r.push(d), r.push(y), r.push(p), r.push(g), r.push(f), r.push(m), r
    },
    getSpeedLine: function (e) {
        return e ? 3 * (1852 * e) / 60 : 0
    },
    getResolution: function () {
        var e = MAPutils.GlobalMap.getCoordinateFromPixel([0, 0]),
            t = MAPutils.GlobalMap.getCoordinateFromPixel(MAPutils.GlobalMap.getSize()), i = t[0] - e[0],
            a = t[1] - e[1], r = MAPutils.GlobalMap.getSize();
        return [Math.abs(i / r[0]), Math.abs(a / r[1])]
    },
    addSelectBorder: function (e) {
        var t = new ol.geom.Point(e), i = new ol.Feature({geometry: t});
        i.setStyle(new ol.style.Style({
            image: new ol.style.Icon({
                size: [30, 30],
                src: "../images/select1.png"
            })
        })), MAPutils.borderselectLayer.getSource().clear(), MAPutils.borderselectLayer.getSource().addFeature(i)
    },
    removeSelectBorder: function () {
        MAPutils.borderselectLayer.getSource().clear()
    },
    getcolor: function (e) {
        return 0 == e ? "#000099" : 1 == e ? "#FFFF00" : 2 == e ? "#3366FF" : 3 == e ? "#FF00CC" : 4 == e ? "#FFFFFF" : "#00FF00"
    },
    parseRote: function (e, t) {
        return (e < 0 || 360 < e) && (e = 0 <= t && t <= 360 ? t : 0), e
    },
    judgeShipType: function () {
        var t = {0: "货船", 1: "油船", 2: "客船", 3: "危险品船", 4: "执法船"};
        return function (e) {
            return t[e] || "其他船"
        }
    }(),
    getShipStyleTest: function (e) {
        var t = "";
        t = null != e.names && "null" != e.names && 2 < e.names.length ? e.names : e.mmsi;
        var i = "#000000";
        if (!e.isShowName) return new ol.style.Style({
            stroke: new ol.style.Stroke({color: i, width: .5}),
            fill: new ol.style.Fill({color: MAPutils.getcolor(e.type)})
        });
        var a = (new ol.format.WKT).readGeometry(e.geom, {
            dataProjection: "EPSG:4326",
            featureProjection: "EPSG:3857"
        }).getCoordinates(), r = MAPutils.GlobalMap.getPixelFromCoordinate(a), o = MAPutils.textSize(e.names), l = {};
        l.left = Math.round(r[0]), l.top = Math.round(r[1]);
        var n = MAPutils.calcLablePos(l, o, e.mmsi), s = [], u = new ol.style.Style({
            stroke: new ol.style.Stroke({color: i, width: .5}),
            fill: new ol.style.Fill({color: MAPutils.getcolor(e.type)})
        });
        if (n && n.canDraw) {
            var c = new ol.style.Text({
                font: "12px Microsoft YaHei",
                text: "" + t,
                offsetY: n.offsetY,
                offsetX: n.offsetX,
                overflow: !0,
                textBaseline: "bottom",
                textAlign: "left",
                backgroundStroke: new ol.style.Stroke({color: "#000000", width: .5}),
                fill: new ol.style.Fill({color: "black"})
            });
            u.setText(c);
            var d = new ol.style.Style({
                fill: new ol.style.Fill({color: "#fc6b00"}),
                stroke: new ol.style.Stroke({color: "black", width: .5})
            }), y = [];
            y[0] = r[0], y[1] = r[1], 0 < n.offsetX && (y[0] += 10, y[1] += n.offsetY), n.offsetX < 0 && (y[0] -= 10, y[1] += n.offsetY);
            var p = MAPutils.GlobalMap.getCoordinateFromPixel(y), g = new ol.geom.LineString([a, p]);
            d.setGeometry(g), s.push(d)
        }
        return s.push(u), s
    },
    clearScreenText: function () {
        for (var e in MAPutils.screenText) delete MAPutils.screenText[e];
        for (var e in MAPutils.spaceCache) delete MAPutils.spaceCache[e]
    },
    calcLablePos: function (e, t, i) {
        for (var a = {}, r = Math.round((e.left + 10) / 30), o = Math.round(e.top / 30) - 1, l = Math.round(t.width / 30) + 1, n = !1, s = r; s < r + l; s++) {
            if ((c = MAPutils.screenText[s + "-" + o]) && c != i) {
                n = !0;
                break
            }
        }
        if (!n) {
            a.canDraw = !0, a.offsetX = 10, a.offsetY = -MAPutils.heigthKey - (e.top - 30 * (o + 1));
            for (var u = r; u < r + l; u++) MAPutils.screenText[u + "-" + o] = i;
            return a
        }
        o = Math.round(e.top / 30), n = !1;
        for (u = r; u < r + l; u++) if ((c = MAPutils.screenText[u + "-" + o]) && c != i) {
            n = !0;
            break
        }
        if (!n) {
            for (a.canDraw = !0, a.offsetX = 10, a.offsetY = -(e.top - 30 * o), u = r; u < r + l; u++) MAPutils.screenText[u + "-" + o] = i;
            return a
        }
        for (o = Math.round(e.top / 30) + 1, n = !1, u = r; u < r + l; u++) if ((c = MAPutils.screenText[u + "-" + o]) && c != i) {
            n = !0;
            break
        }
        if (!n) {
            for (a.canDraw = !0, a.offsetX = 10, a.offsetY = MAPutils.heigthKey - (e.top - 30 * (o - 1)), u = r; u < r + l; u++) MAPutils.screenText[u + "-" + o] = i;
            return a
        }
        for (l = Math.round(t.width / 30), r = Math.round((e.left - t.width - 10) / 30) - 1, o = Math.round(e.top / 30) - 1, n = !1, u = r; u < r + l; u++) if ((c = MAPutils.screenText[u + "-" + o]) && c != i) {
            n = !0;
            break
        }
        if (!n) {
            for (a.canDraw = !0, a.offsetX = -t.width - 10, a.offsetY = -MAPutils.heigthKey - (e.top - 30 * (o + 1)), u = r; u < r + l; u++) MAPutils.screenText[u + "-" + o] = i;
            return a
        }
        for (o = Math.round(e.top / 30), n = !1, u = r; u < r + l; u++) if ((c = MAPutils.screenText[u + "-" + o]) && c != i) {
            n = !0;
            break
        }
        if (!n) {
            for (a.canDraw = !0, a.offsetX = -t.width - 10, a.offsetY = -(e.top - 30 * o), u = r; u < r + l; u++) MAPutils.screenText[u + "-" + o] = i;
            return a
        }
        o = Math.round(e.top / 30) + 1, n = !1;
        for (s = r; s < r + l; s++) {
            var c;
            if ((c = MAPutils.screenText[s + "-" + o]) && c != i) {
                n = !0;
                break
            }
        }
        if (n) return a;
        a.canDraw = !0, a.offsetX = -t.width - 10, a.offsetY = MAPutils.heigthKey - (e.top - 30 * (o - 1));
        for (s = r; s < r + l; s++) MAPutils.screenText[s + "-" + o] = i;
        return a
    },
    getShipStyle: function (e) {
        return new ol.style.Style({
            stroke: new ol.style.Stroke({color: "#000000", width: .5}),
            fill: new ol.style.Fill({color: MAPutils.getcolor(e)})
        })
    },
    getShipStyleByCache: function (e) {
        return null == e && (e = 6), MAPutils.shipGlobalStyle[e] || MAPutils.shipGlobalStyle[6]
    },
    textSize: function (e) {
        var t = {}, i = document.getElementById("_measureWidth");
        return i || ((i = document.createElement("span")).id = "_measureWidth", i.style.cssText = "font: 12px Microsoft YaHei; whiteSpace: nowrap; visibility: hidden", document.body.appendChild(i)), JSTYPE.isUndefined(i.textContent) ? i.innerText = "" : i.textContent = "", t.width = i.offsetWidth, JSTYPE.isUndefined(i.textContent) ? i.innerText = e : i.textContent = e, t.width = i.offsetWidth - t.width, t.height = 22, t
    },
    radioPop: function (a, e, t) {
        var r, i = t || {}, o = '<span style="color:red">无数据</span>',
            l = '<table style="width: 100%;margin-top:0px;" borderColor="#dddddd" cellSpacing="0" cellPadding="0" border="1"><tr height="32" ><td width="30%" align="center">台站地点：</td><td width="80%" align="center">' + (i.names || o) + '</td><tr><tr height="32"><td align="center">频道(ch)：</td><td align="center">' + (i.channel || o) + '</td><tr><tr height="32"><td align="center">覆盖水域：</td><td align="center">' + (i.waterzone || o) + "</td><tr></table>";
        layer.close(MAPutils.DialogIndex), MAPutils.DialogIndex = layer.open({
            type: 1,
            shade: 0,
            shift: MAPutils.getLayerAnim(),
            title: "<B>" + i.names + "</B>-VHF基站",
            area: ["300px", "158px"],
            offset: e,
            content: l,
            success: function (e, t) {
                MAPutils.layerHeightAutoResize(e);
                var i = MAPutils.radioLayer.getSource().getFeatureById(a).get("data");
                r = MAPutils.FeatureAnim.start({
                    id: i.pid,
                    animType: MAPutils.animType.vhf,
                    geom: i.geomare,
                    layerType: "VHFAnimLayer"
                })
            },
            end: function () {
                MAPutils.FeatureAnim.stop(r)
            }
        })
    },
    AISPop: function (e, t, i) {
        var a = i || {}, r = '<span style="color:red">无数据</span>',
            o = '<table style="width: 100%;height:100%" borderColor="#dddddd" cellSpacing="0" cellPadding="0" border="1"><tr height="32" ><td width="40%" align="center">基站名称：</td><td width="80%" align="center">' + (a.name || r) + '</td></tr><tr height="32"><td align="center">所属省份：</td><td align="center">' + (a.province || r) + '</td></tr><tr height="32"><td align="center">设备厂商：</td><td align="center">' + (a.device_manufacturers || r) + "</td></tr></table>";
        layer.close(MAPutils.DialogIndex), MAPutils.DialogIndex = layer.open({
            type: 1,
            shade: 0,
            shift: MAPutils.getLayerAnim(),
            title: "<B>AIS基站信息</B>",
            area: ["300px", "200px"],
            offset: t,
            content: o
        })
    },
    warningPop: function (m, t, h) {
        $.get("html/navigation/warningnotice.html", {}, function (e) {
            layer.close(MAPutils.DialogIndex), MAPutils.DialogIndex = layer.open({
                type: 1,
                shade: 0,
                shift: MAPutils.getLayerAnim(),
                title: "<B>航警弹窗</B>",
                area: "400px",
                offset: t,
                content: e,
                success: function (e, t) {
                    e.find(".layui-layer-title")[0].innerText = " ";
                    var i = h || MAPutils.useLayer("航行通警告").getSource().getFeatureById(m);
                    if (!i) return layer.close(MAPutils.DialogIndex), void layer.msg("页面错误，请稍后再试");
                    var a = i.get("jsonObject");
                    e.find(".notice-title").text(a.title), e.find("#my-table").find("tbody").empty();
                    var r = a.type, o = a.extension, l = [];
                    if (r) {
                        var n = AAWC.getConfig(r);
                        n && (l = n.ExtendProperty)
                    }
                    for (var s = AAWC.translateForm, u = "", c = 0; c < l.length; c++) {
                        var d = o[l[c].name];
                        d = s[d] ? s[d] : d, u += "<tr><th>" + l[c].content + "</th><td>" + d + "</td></tr>"
                    }
                    e.find("#my-table").find("tbody").append($(u));
                    var y = "";
                    e.find("#page-body-author").empty();
                    var p = a.author;
                    y += "<span>" + p.productionAgency + "</span><span>文号:";
                    var g = p.warningType;
                    g = s[g] ? s[g] : d, y += p.nameOfSeries + "</span><span>索引号:" + p.warningNumber + "</span>", y += "<span>发布年份:" + p.year + "</span>", e.find("#page-body-author").append($(y));
                    var f = a.content;
                    _.isArray(f), f = f.join(""), e.find("#page-body-title").text(f)
                }
            })
        })
    },
    guidancePop: function (e, t) {
        layer.close(MAPutils.DialogIndex);
        var i = e.get("data"), a = i.messageType, r = NConfig.model[a], o = r["html-model"], l = _.cloneDeep(i);
        if (l.messageType = r["property-name"], "ChannelTurn" == a && (l.inouttype = "0" == l.inouttype ? "线左侧进港右侧出港" : "线右侧进港左侧出港"), "LimitEntry" == a) {
            var n = l.shiplist;
            if (null == (n = JSON.parse(n)) || 0 == n.length) l.shipList = " "; else {
                for (var s = "", u = 0; u < n.length; u++) s += n[u].mmsi + ",";
                l.shipList = s
            }
        }
        if ("ConditionLimit" == a) {
            var c = {0: "货船", 1: "油船", 2: "客船", 3: "危险品船", 4: "执法船舶", 5: "其它船舶"}, d = l.type.split(",");
            for (s = "", u = 0; u < d.length; u++) s += c[d[u]] + ",";
            l.shipType = s
        }
        s = refreshGeometry.getHtml(o, l);
        MAPutils.DialogIndex = layer.open({
            type: 1,
            shade: 0,
            shift: MAPutils.getLayerAnim(),
            title: "<B>航行规则详情</B>",
            area: "400px",
            offset: t,
            content: s,
            success: function (e, t) {
                table.init("navigation-table", {cols: [[{width: 100}, {}]]})
            }
        })
    },
    naviMarkPop: function (a, r) {
        var e = MAPutils.naviMarkLayer.getSource().getFeatureById(a), u = e ? e.get("data") : {};
        MAPutils.drawFocus({
            id: a,
            layer: MAPutils.naviMarkLayer
        }), $.post("page/getNaviMarkInfo", {id: a}, function (e) {
            var t = e.result;
            if (t) {
                null == t.saveDistance && (t.saveDistance = "");
                var s = '<span style="color:red">无数据</span>',
                    i = '<table style="width: 100%;height:100%" borderColor="#dddddd" cellSpacing="0" cellPadding="0" border="1"><tr height="32" ><td align="center">航标图片：</td><td align="center" id="nm-image">loading...</td><tr><tr height="32" ><td width="30%" align="center">航标编号：</td><td align="center">' + (t.marktablecode || s) + '</td><tr><tr height="32"><td align="center">航标名称：</td><td align="center">' + (t.markname || s) + '</td><tr><tr height="32"><td align="center">航标种类：</td><td align="center">' + (t.typename || s) + '</td><tr><tr height="32"><td align="center">航标功能：</td><td align="center">' + (t.func || s) + '</td><tr><tr height="32"><td align="center">安全距离：</td><td align="center" id="nm-saveDistance">loading...</td><tr><tr height="32"><td align="center">灯质：</td><td align="center" id="nm-light">loading...</td><tr></table>';
                layer.close(MAPutils.DialogIndex), debounce(function () {
                    var n;
                    MAPutils.DialogIndex = layer.open({
                        type: 1,
                        shade: 0,
                        shift: MAPutils.getLayerAnim(),
                        title: "<B>航标信息</B>",
                        area: ["350px", "270px"],
                        offset: r,
                        content: i,
                        end: function () {
                            MAPutils.clearFocus({id: a, layer: MAPutils.naviMarkLayer}), MAPutils.stopFlash(n)
                        },
                        success: function (l, e) {
                            $.post("page/getHttpMarkInfoByName", {markName: t.markname}, function (e) {
                                var t = l.find("#nm-saveDistance"), i = l.find("#nm-light"), a = l.find("#nm-image");
                                if (e) if (n = MAPutils.startFlash({
                                    LightColor: e.LightColor,
                                    LightDetail: e.LightDetail,
                                    LightParameter: e.LightParameter,
                                    geom: u.geom
                                }), e.SaveDistance ? t.html(e.SaveDistance) : t.html(s), e.Light ? i.html(e.Light) : i.html(s), e.MarkImage) {
                                    var r = e.MarkImage.split(","), o = "";
                                    (r = r.filter(function (e, t, i) {
                                        return !JSTYPE.isBlank(e)
                                    })).forEach(function (e, t, i) {
                                        o += '<image class="naviMarkImage" src="' + MAPutils.naviMarkImagePre + e + '">'
                                    }), a.html(o), MAPutils.layerHeightAutoResize(l), layer.photos({
                                        photos: "#nm-image",
                                        move: !1
                                    })
                                } else a.html(s); else t.html(s), i.html(s), a.html(s)
                            })
                        }
                    })
                })
            } else layer.msg("无数据", {icon: 5, anim: 6})
        })
    },
    aisMarkPop: function (e, t) {
        var i = e.mmsi;
        MAPutils.drawFocus({id: i, layer: MAPutils.naviMarkLayer});
        var a = '<span style="color:red">无数据</span>',
            r = '<table style="width: 100%;height:100%" borderColor="#dddddd" cellSpacing="0" cellPadding="0" border="1"><tr height="32"><td align="center">航标名称：</td><td align="center">' + (e.names || e.enames || a) + '</td><tr><tr height="32"><td align="center">虚拟航标：</td><td align="center">' + (0 == e.isvirtual ? "否" : "是") + '</td><tr><tr height="32"><td align="center">更新时间：</td><td align="center">' + (e.updatetime || a) + "</td><tr></table>";
        layer.close(MAPutils.DialogIndex), debounce(function () {
            MAPutils.DialogIndex = layer.open({
                type: 1,
                shade: 0,
                shift: MAPutils.getLayerAnim(),
                title: "<B>AIS航标信息</B>",
                area: "350px",
                offset: t,
                content: r,
                end: function () {
                    MAPutils.clearFocus({id: i, layer: MAPutils.naviMarkLayer})
                }
            })
        })
    },
    weatherPop: function (e, t) {
        var i = MAPutils.weatherLayer.getSource().getFeatureById(e).get("data"),
            a = '<span style="color:red">无数据</span>',
            r = '<table style="width: 100%;" borderColor="#dddddd" cellSpacing="0" cellPadding="0" border="1"><tr height="30" ><td width="30%" align="center">天气：</td><td width="70%" align="center">' + (i.sk_s || a) + '</td><tr><tr height="30"><td align="center">温度：</td><td align="center">' + (i.sk_t ? i.sk_t + "℃" : a) + '</td><tr><tr height="30"><td align="center">相对湿度：</td><td align="center">' + (i.sk_h || a) + '</td><tr><tr height="30"><td align="center">气压：</td><td align="center">' + (i.sk_p ? i.sk_p + "HPA" : a) + '</td><tr><tr height="30"><td align="center">风向：</td><td align="center">' + (i.sk_wd ? i.sk_wd + "度" : a) + '</td><tr><tr height="30"><td align="center">风速：</td><td align="center">' + (i.sk_wp ? i.sk_wp + "米/秒" : a) + '</td><tr><tr height="30"><td align="center">时间：</td><td align="center">' + (i.sk_time || a) + "</td><tr></table>";
        layer.close(MAPutils.DialogIndex), MAPutils.DialogIndex = layer.open({
            type: 1,
            shade: 0,
            shift: MAPutils.getLayerAnim(),
            title: "<B>" + i.n + "</B>-气象",
            area: ["220px", "265px"],
            offset: t,
            content: r
        })
    },
    shipPop: function (i, t) {
        MAPutils.drawFocus({id: i}), WATHER.listen("refreshShip", function () {
            MAPutils.drawFocus({id: i})
        }), $.get("html/shipLayer.html", function (e) {
            var r = MAPutils.shipLayer.getSource().getFeatureById(i).get("data");
            e = MAPutils.formatStr(e, {
                mmsi: r.mmsi || "未知",
                names: r.names || "未知",
                type: MAPutils.judgeShipType(r.type),
                thg: "511" == r.thg ? "未知" : r.thg + "度",
                cog: r.cog.toFixed(2),
                sog: r.sog.toFixed(2),
                length: r.length && r.length + "米" || "未知",
                width: r.width && r.width + "米" || "未知",
                lat: MAPutils.ChangeToDFM(r.lat),
                long: MAPutils.ChangeToDFM(r.lon)
            }), layer.close(MAPutils.DialogIndex), MAPutils.DialogIndex = layer.open({
                type: 1,
                title: "<B>船舶详情:" + (r.names ? r.names : r.mmsi) + "</B>",
                content: e,
                shade: 0,
                offset: t,
                area: ["450px", "310px"],
                end: function (e, t) {
                    MAPutils.clearFocus({id: i}), WATHER.remove("refreshShip")
                },
                success: function (e, t) {
                    MAPutils.layerHeightAutoResize(e), e.on("click", "#shipPop-dest", function () {
                        var a = !0;
                        layer.load(), setTimeout(function () {
                            a && (a = !1, layer.closeAll("loading"), layer.msg("此船舶最近无进出港口记录！", {icon: 5, anim: 6}))
                        }, MAPutils.allowRequestTime), $.ajax({
                            url: "ship/getGkRecByMmsi",
                            type: "post",
                            dataType: "json",
                            data: {mmsi: r.mmsi},
                            success: function (e) {
                                if (layer.closeAll("loading"), a) if (a = !1, e && e.result && "false" != e.result) {
                                    var t = "";
                                    e.result.forEach(function (e) {
                                        t += "<tr><td>" + (e.zonename || "未知") + "</td><td>" + (e.entertime || "未知") + "</td><td>" + (e.leavetime || "未知") + "</td></tr>"
                                    });
                                    var i = '<table class="layui-table"><thead><tr><th>港口</th><th>进入时间</th><th>离开时间</th></tr></thead><tbody>' + t + "</tbody></table>";
                                    layer.open({
                                        type: 1,
                                        title: "<B>最近进出港口 - " + r.names + "</B>",
                                        content: i,
                                        shade: .6,
                                        shadeClose: !0,
                                        area: ["720px", "300px"]
                                    })
                                } else layer.msg("此船舶最近无进出港口记录！", {icon: 5, anim: 6})
                            }
                        })
                    }), e.on("click", "#WGA_button_add_fleet", function (e) {
                        if (WGAuth.click("button", "WGA_button_add_fleet")) {
                            e.preventDefault();
                            var t = r.mmsi;
                            $.ajax({
                                url: "fleet/checkMyFleet",
                                type: "post",
                                dataType: "json",
                                data: {mmsi: t},
                                success: function (e) {
                                    e ? $.ajax({
                                        url: "fleet/addToMyFleet",
                                        type: "post",
                                        dataType: "json",
                                        data: {mmsi: t},
                                        success: function (e) {
                                            e ? layer.msg("添加成功，请在我的船队中查看!", {icon: 6}) : layer.msg("添加失败!", {
                                                icon: 5,
                                                anim: 6
                                            })
                                        },
                                        error: function () {
                                            layer.msg("添加失败!", {icon: 5, anim: 6})
                                        }
                                    }) : layer.msg("当前船舶已存在!", {icon: 5, anim: 6})
                                }
                            })
                        }
                    }), e.on("click", "#WGA_button_24hour_guiji", function () {
                        MAPutils.drawLineByMmsi(i, {name: r.names || r.mmsi})
                    }), e.on("click", "#WGA_button_3day_water", function () {
                        MAPutils.waterCrp(r.mmsi)
                    })
                }
            })
        })
    },
    hydroPop: function (e, t) {
        var i = e.station.name, a = e.hydroMeteorData || {}, r = (e.time, this.hydroFilter(a));
        if (!r) return layui.layer.msg("无最新的水文气象数据！", {icon: 5}), !1;
        var o = '<table style="width: 100%;height:100%" borderColor="#dddddd" cellSpacing="0" cellPadding="0" border="1">';
        o += r, o += "</table>", layer.close(MAPutils.DialogIndex), MAPutils.DialogIndex = layer.open({
            type: 1,
            shade: 0,
            shift: MAPutils.getLayerAnim(),
            title: "水文气象 - " + i,
            area: "270px",
            offset: t,
            content: o
        })
    },
    waterCrp: function (e) {
        if (WGAuth.click("button", "WGA_button_3day_water")) {
            var t = !0;
            layer.load(), setTimeout(function () {
                if (t) {
                    if (!WGAuth.click("button", "WGA_button_3day_water")) return;
                    t = !1, layer.closeAll("loading"), layer.msg("暂无水域变化报告数据！", {icon: 5, anim: 6})
                }
            }, MAPutils.allowRequestTime), $.ajax({
                url: "ship/waterCrpThree",
                type: "post",
                dataType: "json",
                data: {mmsi: e},
                success: function (r) {
                    layer.closeAll("loading"), t && (t = !1, r && "false" != r && "false" != r.data && r.data.length ? (0 != MAPutils.waterLayer && layer.close(MAPutils.waterLayer), $.get("html/waterLayer.html", function (e) {
                        MAPutils.waterLayer = layer.open({
                            type: 1,
                            title: "3天水域变化报告",
                            area: ["600px", "300px"],
                            content: e
                        });
                        for (var t = r.data, i = "", a = 0; a < t.length; a++) i += " <tr>", i += "        <td>" + t[a].mmsi + "</td>", i += "        <td>" + t[a].names + "</td>", i += "        <td>" + t[a].wznames + "</td>", i += "        <td>" + t[a].entertime + "</td>", i += "    </tr>";
                        $("#waterTb tbody").html(i)
                    })) : layer.msg("暂无水域变化报告数据！", {icon: 5, anim: 6}))
                }
            })
        }
    },
    FeatureAnim: function () {
        var c, d, y, p, g = {};
        return c = function () {
            return (new Date).getTime()
        }, d = function (e, t) {
            var i = !1;
            return t <= c() - e && (i = !0), i
        }, p = function (e) {
            for (var t in g) new RegExp(e.animType).test(t) && y({layerType: e.layerType, animID: t})
        }, {
            start: function (e) {
                var i, a = 0, r = $.extend({
                    id: null,
                    animType: "VHFAnim",
                    geom: null,
                    layerType: "VHFAnimLayer",
                    minScale: 0,
                    maxScale: 1,
                    scaleStep: .01,
                    style: null,
                    styleFn: null,
                    delay: null,
                    twinkleTimes: null,
                    closeSameType: !1
                }, e);
                r.closeSameType && p({animType: r.animType, layerType: r.layerType});
                var o = r.minScale, l = r.maxScale - r.minScale, n = r.animType + r.id;
                g[n] = !0;
                var s = {layerType: r.layerType, animID: n}, t = (new ol.format.WKT).readGeometry(r.geom, {
                    dataProjection: "EPSG:4326",
                    featureProjection: "EPSG:3857"
                });
                t.scale(o, o);
                var u = new ol.Feature({geometry: t});
                return u.setId(n), u.set("layerType", r.layerType), u.set("animFeature", !0), u.set("origionID", r.id), r.style && !r.styleFn && u.setStyle(r.style), r.styleFn && r.styleFn({
                    feature: u,
                    scale: o,
                    minScale: r.minScale,
                    maxScale: r.maxScale,
                    scalePercent: 0
                }), MAPutils[r.layerType].getSource().addFeature(u), function t() {
                    o > r.maxScale ? (o = r.minScale, a += 1) : o += r.scaleStep, MAPutils.requestAnimationFrame(function () {
                        var e = (new ol.format.WKT).readGeometry(r.geom, {
                            dataProjection: "EPSG:4326",
                            featureProjection: "EPSG:3857"
                        });
                        e.scale(o, o), u.setGeometry(e), r.styleFn && r.styleFn({
                            feature: u,
                            scale: o,
                            minScale: r.minScale,
                            maxScale: r.maxScale,
                            scalePercent: (o - r.minScale) / l
                        }), g[n] && (r.twinkleTimes && a >= r.twinkleTimes ? y(s) : r.delay && d(i, r.delay) ? y(s) : t())
                    })
                }(), i = c(), s
            }, stop: y = function (e) {
                delete g[e.animID];
                var t = MAPutils[e.layerType].getSource();
                t.removeFeature(t.getFeatureById(e.animID))
            }
        }
    }(),
    startLineAnim: function (e) {
        var i, a, r, o, l, n = MAPutils.locatAnimPre + (new Date).getTime(), s = 60, u = 1, c = .01, d = 0;
        return i = $.extend({
            layer: null,
            geom: null,
            color: [255, 22, 0, 1],
            width: 3,
            times: 4,
            widthIncrease: .3
        }, e), MAPutils.stopLocatAnim({layer: i.layer}), MAPutils.switchLayer(i.layer, !0), MAPutils.locatAnimStatus[n] = !0, r = {
            id: n,
            layer: i.layer
        }, i.color[3] ? i.color[3] < .8 && (i.color[3] = .8) : i.color[3] = 1, o = i.width, l = i.color.slice(0), a = MAPutils.drawVectorFeature(i.layer, {
            id: n,
            style: new ol.style.Style({stroke: new ol.style.Stroke({color: l, width: o})}),
            geom: i.geom
        }), function e() {
            if (MAPutils.locatAnimStatus[n]) if (s < u && (d++, u = 0, o = i.width, l = i.color.slice(0)), d >= i.times) MAPutils.stopLocatAnim(r); else {
                u++, o += i.widthIncrease, l[3] -= c;
                var t = new ol.style.Style({stroke: new ol.style.Stroke({color: l, width: o})});
                a.setStyle(t), MAPutils.requestAnimationFrame(e)
            }
        }(), r
    },
    startPolygonAnim: function (e) {
        var r, o, t, l, i, a, n, s, u = MAPutils.locatAnimPre + (new Date).getTime(), c = .01, d = .01, y = 0;
        .62 < (a = (r = $.extend({
            layer: null,
            geom: null,
            color: [255, 22, 0, 1],
            times: 4,
            minScale: 1,
            maxScale: 1.4,
            type: 1
        }, e)).maxScale - r.minScale) ? console.error("最大最小缩放比例的差不得大于0.6") : (MAPutils.stopLocatAnim({layer: r.layer}), MAPutils.switchLayer(r.layer, !0), MAPutils.locatAnimStatus[u] = !0, o = {
            id: u,
            layer: r.layer
        }, r.color[3] ? r.color[3] < .8 && (r.color[3] = .8) : r.color[3] = 1, s = r.color.slice(0), n = r.color.slice(0), scale = r.minScale, 2 == r.type && (d = c = -.01, s[3] -= a, n[3] -= a, scale = r.maxScale), (t = (new ol.format.WKT).readGeometry(r.geom, {
            dataProjection: "EPSG:4326",
            featureProjection: "EPSG:3857"
        })).scale(scale, scale), l = new ol.Feature({geometry: t}), i = new ol.style.Style({
            stroke: new ol.style.Stroke({color: n}),
            fill: new ol.style.Fill({color: n})
        }), l.setId(u), l.setStyle(i), r.layer.getSource().addFeature(l), function e() {
            var t;
            if (MAPutils.locatAnimStatus[u]) if (n[3] -= d, t = scale, (1 == r.type ? t > r.maxScale : t < r.minScale) ? (n = r.color.slice(0), scale = 1 == r.type ? r.minScale : r.maxScale, y += 1) : scale += c, y >= r.times) MAPutils.stopLocatAnim(o); else {
                var i = new ol.style.Style({
                    stroke: new ol.style.Stroke({color: n}),
                    fill: new ol.style.Fill({color: n})
                }), a = (new ol.format.WKT).readGeometry(r.geom, {
                    dataProjection: "EPSG:4326",
                    featureProjection: "EPSG:3857"
                });
                a.scale(scale, scale), l.setGeometry(a), l.setStyle(i), MAPutils.requestAnimationFrame(e)
            }
        }())
    },
    stopLocatAnim: function (e) {
        var t = e.layer, i = e.id;
        if (t && i) {
            var a = (r = e.layer.getSource()).getFeatureById(e.id);
            a && r.removeFeature(a), delete MAPutils.locatAnimStatus[i]
        } else if (t && !i) for (var r, o = (r = e.layer.getSource()).getFeatures(), l = 0, n = o.length; l < n; l++) {
            var s = o[l], u = s.getId();
            u && _.startsWith(u, MAPutils.locatAnimPre) && (r.removeFeature(s), delete MAPutils.locatAnimStatus[u])
        }
    },
    startLocatAnim: function (e) {
        if (_.startsWith(e.geom, "LINESTRING")) MAPutils.startLineAnim(e); else if (_.startsWith(e.geom, "POLYGON")) MAPutils.startPolygonAnim(e); else if (_.startsWith(e.geom, "POINT")) {
            var t = MAPutils.WKT.readGeometry(e.geom).getCoordinates();
            MAPutils.twinkle(t)
        }
    },
    DrawTemp: function () {
        var i = {drawType: {1: "Point", 2: "Circle", 3: "Polygon", 4: "LineString"}, drawInteraction: null}, u = {
            start: function (e) {
                var s = $.extend({
                    title: null,
                    featureTitle: null,
                    type: 1,
                    regularPolygon: null,
                    box: !1,
                    drawStyle: CONFIG.style.draw,
                    endStyle: null,
                    cb: null
                }, e);
                u.cancel();
                var t = MAPutils.useLayer(s.title);
                t && (s.box ? i.drawInteraction = new ol.interaction.Draw({
                    source: t.getSource(),
                    type: "Circle",
                    style: s.drawStyle,
                    geometryFunction: ol.interaction.Draw.createBox()
                }) : s.regularPolygon ? i.drawInteraction = new ol.interaction.Draw({
                    source: t.getSource(),
                    type: "Circle",
                    style: s.drawStyle,
                    geometryFunction: ol.interaction.Draw.createRegularPolygon(s.regularPolygon)
                }) : i.drawInteraction = new ol.interaction.Draw({
                    source: t.getSource(),
                    type: i.drawType[s.type],
                    style: s.drawStyle
                }), MAPutils.GlobalMap.addInteraction(i.drawInteraction), i.drawInteraction.on("drawend", function (e) {
                    var t, i, a, r, o, l, n = {};
                    t = e.feature, s.featureTitle && t.set("title", s.featureTitle), u.cancel(), t.setStyle(s.endStyle || s.drawStyle), (i = t.getGeometry()) instanceof ol.geom.Circle ? (r = (new ol.format.WKT).writeGeometry(new ol.geom.Point(i.getCenter()).transform("EPSG:3857", "EPSG:4326")), n.radius = i.getRadius()) : (r = (new ol.format.WKT).writeFeature(t, {
                        dataProjection: "EPSG:4326",
                        featureProjection: "EPSG:3857"
                    }), i instanceof ol.geom.Point && (a = i.getCoordinates(), o = MAPutils.changeToLonLat(a), l = [MAPutils.ChangeToDFMArr(o[0]), MAPutils.ChangeToDFMArr(o[1])], o = [MAPutils.ChangeToDFM(o[0]), MAPutils.ChangeToDFM(o[1])], n.lonlat = o, n.lonlatArr = l)), n.feature = t, n.geomStr = r, s.cb && s.cb(n)
                }))
            }, cancel: function () {
                MAPutils.GlobalMap.removeInteraction(i.drawInteraction)
            }
        };
        return {start: u.start, cancel: u.cancel}
    }(),
    Modify: function () {
        var e = function (e) {
            this.feature = e.feature, this.cb = e.cb, this.interaction = null, this.init()
        };
        return e.prototype.init = function () {
            var t = this;
            if (!(t.feature instanceof ol.Collection)) {
                var e = new ol.Collection;
                e.push(t.feature), t.feature = e
            }
            t.interaction = new ol.interaction.Modify({features: t.feature}), MAPutils.GlobalMap.addInteraction(t.interaction), t.interaction.on("modifyend", function (e) {
                t.cb && t.cb(e)
            })
        }, e.prototype.cancel = function () {
            MAPutils.GlobalMap.removeInteraction(this.interaction)
        }, e
    }(),
    drawFocus: function (e) {
        var t, i, a;
        if (!(i = (t = $.extend({
            id: null,
            layer: MAPutils.shipLayer,
            type: 1,
            drawLayer: MAPutils.focusLayer
        }, e)).layer.getSource().getFeatureById(t.id))) return !1;
        if (MAPutils.clearFocus({id: t.id, layer: t.layer, drawLayer: t.drawLayer}), 1 == t.type) {
            if (!(a = i.get("data") && i.get("data").geom)) return !1;
            MAPutils.drawVectorFeature(t.drawLayer, {
                geom: a,
                id: t.layer.get("title") + t.id,
                style: new ol.style.Style({image: new ol.style.Icon({src: "image/square/all.png"})})
            })
        }
    },
    clearFocus: function (e) {
        var t, i, a;
        i = (t = $.extend({
            id: null,
            layer: MAPutils.shipLayer,
            drawLayer: MAPutils.focusLayer
        }, e)).layer.get("title") + t.id, (a = t.drawLayer.getSource().getFeatureById(i)) && t.drawLayer.getSource().removeFeature(a)
    },
    startFlash: function (i) {
        var c, d, y = 0, p = 1, g = 50, f = "flashAnim" + (new Date).getTime();
        if (c = {
            "红": [255, 69, 0, p],
            "黄": [255, 255, 0, p],
            "绿": [0, 255, 0, p],
            "蓝": [24, 116, 205, p],
            "白": [248, 248, 255, p]
        }, !((i = i || {}).LightColor && i.LightDetail && i.LightParameter && i.geom)) return !1;
        if ("其他" == i.LightDetail) return !1;
        if (-1 == Object.keys(c).indexOf(i.LightColor)) return !1;
        if ((d = (d = i.LightDetail.split("+")).filter(function (e) {
            return !!e && !isNaN(+e)
        })).length) return MAPutils.flashAnimStatus[f] = !0, m(), f;

        function m() {
            if (MAPutils.flashAnimStatus[f]) {
                y >= d.length && (y = 0);
                var e = 1e3 * d[y];
                if (y % 2) setTimeout(function () {
                    m()
                }, e); else {
                    var a = 0, r = Math.ceil(60 * e / 1e3), o = r, l = 2 * g / (r * r), n = (p - .8) / r,
                        s = c[i.LightColor].slice(0), t = MAPutils.geom2Coor(i.geom),
                        u = new ol.Feature({geometry: new ol.geom.Point(t)});
                    u.setId(f), MAPutils.flashLayer.getSource().addFeature(u), function e() {
                        if (MAPutils.flashAnimStatus[f]) {
                            var t = o - r;
                            a = l * t * t / 2, s[3] = s[3] - n;
                            var i = new ol.style.Style({
                                image: new ol.style.Circle({
                                    radius: a,
                                    fill: new ol.style.Fill({color: s})
                                })
                            });
                            u.setStyle(i), --r ? MAPutils.requestAnimationFrame(e) : (MAPutils.flashLayer.getSource().removeFeature(u), m())
                        }
                    }()
                }
                y++
            }
        }
    },
    stopFlash: function (e) {
        e && (MAPutils.flashAnimStatus[e] && delete MAPutils.flashAnimStatus[e], MAPutils.flashLayer.getSource().clear())
    },
    ChangeToDFM: function (e) {
        var t = Math.abs(e), i = Math.floor(t), a = Math.floor(60 * (t - i)), r = (3600 * (t - i - a / 60)).toFixed(2);
        return (i *= e / t) + "°" + a + "'" + r + '"'
    },
    ChangeToDFMArr: function (e) {
        var t = Math.abs(e), i = Math.floor(t), a = Math.floor(60 * (t - i)), r = (3600 * (t - i - a / 60)).toFixed(2);
        return [i *= e / t, a, r]
    },
    ChangeToDu: function (e, t, i) {
        var a;
        return e = +e, t = Math.abs(+t), i = Math.abs(+i), ((a = Math.abs(e)) + t / 60 + i / 3600) * (a / e)
    },
    changeToLonLat: function (e, t) {
        return ol.proj.toLonLat(e, t || "EPSG:3857")
    },
    coor2lonlatStr: function (e, t) {
        var i = MAPutils.changeToLonLat(e, t), a = i[0], r = i[1];
        return [Math.abs(a) + (0 < a ? "E" : "W"), Math.abs(r) + (0 < r ? "N" : "S")]
    },
    wkt2lonlatArr: function (e) {
        var t = this.WKT.readGeometry(e).getCoordinates(), i = MAPutils.changeToLonLat(t);
        return [MAPutils.ChangeToDFMArr(i[0]), MAPutils.ChangeToDFMArr(i[1])]
    },
    wkt2lonlatStr: function (e) {
        var t = this.wkt2lonlatArr(e);
        return [t[0][0] + "°" + t[0][1] + "'" + t[0][2] + '"', t[1][0] + "°" + t[1][1] + "'" + t[1][2] + '"']
    },
    WKT: function () {
        var e = function () {
            this.wkt = new ol.format.WKT, this.options = {dataProjection: "EPSG:4326", featureProjection: "EPSG:3857"}
        };
        return e.prototype.readFeature = function (e) {
            return this.wkt.readFeature(e, this.options)
        }, e.prototype.readFeatures = function (e) {
            return this.wkt.readFeatures(e, this.options)
        }, e.prototype.readGeometry = function (e) {
            return this.wkt.readGeometry(e, this.options)
        }, e.prototype.writeFeature = function (e) {
            return this.wkt.writeFeature(e, this.options)
        }, e.prototype.writeFeatures = function (e) {
            return this.wkt.writeFeatures(e, this.options)
        }, e.prototype.writeGeometry = function (e) {
            return this.wkt.writeGeometry(e, this.options)
        }, new e
    }(),
    getCenterById: function (e, t) {
        return MAPutils.getFeatureCenter(e.getSource().getFeatureById(t))
    },
    getPopOffSet: function () {
        var e = document.documentElement.clientWidth || document.body.clientWidth,
            t = document.documentElement.clientHeight || document.body.clientHeight;
        return [parseInt(t / 2) - 30 + "px", parseInt(e / 2) + 20 + "px"]
    },
    getOverlayByType: function (e, o) {
        var t = MAPutils.GlobalMap.getOverlays().getArray(), i = function (r) {
            return t.filter(function (e, t, i) {
                var a = e.get("layerType") === r;
                return a && o && o(e, a), a
            })
        };
        if (JSTYPE.isString(e)) return i(e);
        if (JSTYPE.isArray(e)) {
            var a = [];
            return e.forEach(function (e) {
                a = a.concat(i(e))
            }), a
        }
    },
    switchLayer: function (e, t) {
        var i = e.get("title");
        i ? MAPutils.switchLayerByTitle(i, !0) : e.setVisible(t)
    },
    switchLayerByTitle: function (e, t) {
        if (_.isArray(e)) e.forEach(function (e) {
            MAPutils.switchLayerByTitle(e, t)
        }); else {
            var i = MAPutils.getLayerByTitle(e), a = i.get("layerType");
            i.setVisible(t), t ? MAPutils.getOverlayByType(a, function (e) {
                e.setPosition(e.get("oldPosition"))
            }) : MAPutils.getOverlayByType(a, function (e) {
                e.setPosition(void 0)
            })
        }
    },
    DouglasPeuker: function (e, t) {
        if (t = t || 10, !e) return console.error("【DouglasPeuker方法】 非法参数，coordinate为空！"), null;
        if (e.length <= 2) return console.warn("【DouglasPeuker方法】 coordinate长度小于2，不会进行抽稀操作！"), e;
        _.forEach(e, function (e, t) {
            e.id = t
        });
        var i = this.compressLine(e, [], 0, e.length - 1, t);
        return i.push(e[0]), i.push(e[e.length - 1]), i.sort(function (e, t) {
            return e.id < t.id ? -1 : e.id > t.id ? 1 : 0
        })
    },
    compressLine: function (e, t, i, a, r) {
        if (i < a) {
            for (var o = 0, l = 0, n = e[i], s = e[a], u = i + 1; u < a; u++) {
                var c = this.distToSegment(n, s, e[u]) / this.GlobalMap.getView().getResolution();
                o < c && (o = c, l = u)
            }
            r <= o && (t.push(e[l]), this.compressLine(e, t, i, l, r), this.compressLine(e, t, l, a, r))
        }
        return t
    },
    distToSegment: function (e, t, i) {
        var a = Math.abs(this.calculationDistance(e, t)), r = Math.abs(this.calculationDistance(e, i)),
            o = Math.abs(this.calculationDistance(t, i)), l = (a + r + o) / 2;
        return 2 * Math.sqrt(Math.abs(l * (l - a) * (l - r) * (l - o))) / a
    },
    calculationDistance: function (e, t) {
        var i = e.latitude, a = t.latitude, r = e.longitude, o = t.longitude, l = i * Math.PI / 180,
            n = a * Math.PI / 180, s = l - n, u = r * Math.PI / 180 - o * Math.PI / 180;
        return 6370996.81 * (2 * Math.asin(Math.sqrt(Math.pow(Math.sin(s / 2), 2) + Math.cos(l) * Math.cos(n) * Math.pow(Math.sin(u / 2), 2))))
    },
    getArrowLineStyle: function (d) {
        d = d || {};
        var i = $.extend({color: d.color || [255, 20, 147, 1], width: d.width || 3}, d);
        return function (e, l) {
            var t = e.getGeometry(), n = [new ol.style.Style({stroke: new ol.style.Stroke(i)})], s = d.minPixel || 50,
                u = 0, c = null;
            return t.forEachSegment(function (e, t) {
                var i = t[0] - e[0], a = t[1] - e[1], r = [(e[0] + t[0]) / 2, (e[1] + t[1]) / 2], o = Math.atan2(a, i);
                c && (u += (MAPutils.calTwoPointsDistance(e, r) + MAPutils.calTwoPointsDistance(e, c)) / l), (s <= u || !c) && (u = 0, n.push(new ol.style.Style({
                    geometry: new ol.geom.Point(r),
                    image: new ol.style.Icon({
                        src: d.img || "image/common/arrow-red.png",
                        scale: d.scale || .6,
                        anchor: [.75, .5],
                        rotateWithView: !0,
                        rotation: -o
                    })
                }))), c = r
            }), n
        }
    },
    getLineStyle: function () {
        return new ol.style.Style({stroke: new ol.style.Stroke({color: "#009587", width: 3, lineDash: [3, 5]})})
    },
    changeServe: function (e, t, i) {
        JSTYPE.isArray(e) ? e.forEach(function (e) {
            MAPutils.ServeUtil.changeCkState(e, t), MAPutils.ServeUtil.getPage("input[title=" + e + "]").prop("checked", t)
        }) : JSTYPE.isString(e) && (MAPutils.ServeUtil.changeCkState(e, t), MAPutils.ServeUtil.getPage("input[title=" + e + "]").prop("checked", t)), i && i()
    },
    getFeatureCenter: function (e) {
        var t;
        if (JSTYPE.isString(e) && (t = MAPutils.WKT.readGeometry(e)), e instanceof ol.Feature && (t = e.getGeometry()), t) return t instanceof ol.geom.Point ? t.getCoordinates() : t instanceof ol.geom.Polygon ? t.getInteriorPoint().getCoordinates() : t instanceof ol.geom.MultiPolygon ? t.getInteriorPoints().getFirstCoordinate() : t instanceof ol.geom.LineString ? t.getCoordinateAt(.5) : t instanceof ol.geom.MultiLineString ? t.getFirstCoordinate() : t instanceof ol.geom.Circle ? t.getCenter() : void 0
    },
    getLineStartAndEnd: function (e) {
        if (/^LINESTRING\(/.test(e)) {
            var t = e.indexOf("(") + 1, i = e.indexOf(")"), a = e.slice(t, i).split(",");
            return {start: "POINT(" + a[0] + ")", end: "POINT(" + a.pop() + ")"}
        }
    },
    getShipFeatureArrInPort: function (e) {
        var t = MAPutils.shipLayer.getSource().getFeatures(), i = e.getGeometry(), a = [];
        return t && (a = t.filter(function (e) {
            return !_.startsWith(e.getId(), "label") && i.intersectsCoordinate(e.get("geom"))
        })), a
    },
    getLayerPixel: function (e) {
        return [e.pixel[1] - 30 + "px", e.pixel[0] + 20 + "px"]
    },
    getMapDivOffset: function () {
        return [60, parseInt($("#left-nav").css("left")) + $("#left-nav").width()]
    },
    getPopOffsetUnderSearch: function () {
        var e = MAPutils.getMapDivOffset();
        return e = [e[0] + 50 + "px", e[1] + 46 + "px"]
    },
    getViewExtend: function () {
        var e = MAPutils.GlobalMap.getView().calculateExtent(),
            t = ol.proj.transform([e[0], e[1]], "EPSG:3857", "EPSG:4326"),
            i = ol.proj.transform([e[2], e[3]], "EPSG:3857", "EPSG:4326");
        return {left: t[0], bottom: t[1], right: i[0], top: i[1]}
    },
    getViewPolygon: function () {
        var e = MAPutils.GlobalMap.getView().calculateExtent(),
            t = ol.proj.transform([e[0], e[1]], "EPSG:3857", "EPSG:4326"),
            i = ol.proj.transform([e[2], e[3]], "EPSG:3857", "EPSG:4326"), a = ["POLYGON(("], r = [];
        return r.push(t[0] + " " + t[1]), r.push(i[0] + " " + t[1]), r.push(i[0] + " " + i[1]), r.push(t[0] + " " + i[1]), r.push(t[0] + " " + t[1]), a.push(r.join(",")), a.push("))"), a.join("")
    },
    calTwoPointsDistance: function (e, t) {
        return Math.sqrt(Math.pow(e[0] - t[0], 2) + Math.pow(e[1] - t[1], 2))
    },
    formatStr: function (e, i) {
        return e.replace(/\$\{(\w+)\}/g, function (e, t) {
            return i[t]
        })
    },
    getLayerAnim: function () {
        return 0
    },
    proxyFn: function (e) {
        var t = Array.prototype.slice.call(arguments, 1);
        return function () {
            e.apply(this, t)
        }
    },
    arrDisRepeat: function (e) {
        return e.filter(function (e, t, i) {
            return t == i.indexOf(e)
        })
    },
    twoDimensionArrDisRepeat: function (e) {
        for (var t = {}, i = [], a = 0, r = e.length; a < r; a++) t[e[a]] || (i.push(e[a]), t[e[a]] = !0);
        return i
    },
    ObjArrDisRepeat: function (e, t) {
        for (var i, a = e.length, r = [], o = 0; o < a; o++) if (0 == (i = r.length)) r.push(e[o]); else for (var l; l < i; l++) t.call(null, e[o], r[l]) || r.push(e[o]);
        return r
    },
    isObjEqual: function (e, t) {
        return JSON.stringify(e) === JSON.stringify(t)
    },
    geom2Coor: function (e) {
        return (new ol.format.WKT).readGeometry(e, {
            dataProjection: "EPSG:4326",
            featureProjection: "EPSG:3857"
        }).getCoordinates()
    },
    getPointsExtent: function (e) {
        var t = [], i = [];
        _.forEach(e, function (e) {
            t.push(e[0]), t.push(e[2]), i.push(e[1]), i.push(e[3])
        });
        var a = Math.min.apply(null, t), r = Math.max.apply(null, t);
        return [a, Math.min.apply(null, i), r, Math.max.apply(null, i)]
    },
    getCoorExtent: function (e) {
        var t = [], i = [];
        _.forEach(e, function (e) {
            t.push(e[0]), i.push(e[1])
        });
        var a = Math.min.apply(null, t), r = Math.max.apply(null, t);
        return [a, Math.min.apply(null, i), r, Math.max.apply(null, i)]
    },
    requestAnimationFrame: function (e) {
        window.requestAnimationFrame ? MAPutils.requestAnimationFrame = function (e) {
            window.requestAnimationFrame(e)
        } : MAPutils.requestAnimationFrame = function (e) {
            setTimeout(e, 1e3 / 60)
        }, MAPutils.requestAnimationFrame(e)
    },
    cancelAnimationFrame: function (e) {
        window.cancelAnimationFrame ? MAPutils.cancelAnimationFrame = function (e) {
            window.cancelAnimationFrame(e)
        } : MAPutils.cancelAnimationFrame = function (e) {
            clearTimeout(e)
        }, MAPutils.cancelAnimationFrame(e)
    },
    layerHeightAutoResize: function (e) {
        e.css("height", "").children(".layui-layer-content").css("height", "")
    },
    hydroFilter: function (e) {
        var t = "";
        return (e = e || {}).time && (t += "<tr  ><td >时间：</td><td >" + e.time + "</td></tr>"), e.tide && 0 != e.tide && 9999 != e.tide && 9998 != e.tide && (t += "<tr ><td >潮位：</td><td >" + e.tide + "厘米</td></tr>"), e.visibility && 0 != e.visibility && 99.99 != e.visibility && 99.98 != e.visibility && (t += '<tr height="32" ><td width="30%" align="right">能见度：</td><td align="center">' + e.visibility + "公里</td></tr>"), e.windSpeed && 0 != e.windSpeed && 99.8 != e.windSpeed && 99.9 != e.windSpeed && (t += "<tr ><td >风速：</td><td >" + e.windSpeed + "米/秒</td></tr>"), e.windDrection && 0 != e.windDrection && 998 != e.windDrection && 999 != e.windDrection && (t += "<tr  ><td >风向：</td><td >" + e.windDrection + "度</td></tr>"), t
    },
    clearDraw: function (t) {
        var e = MAPutils.featureFilter(t, function (e) {
            return e.get("featureType") == CONFIG.DRAW_TYPE
        });
        _.forEach(e, function (e) {
            t.getSource().removeFeature(e)
        })
    },
    featureFilter: function (e, t) {
        return e.getSource().getFeatures().filter(t)
    },
    refreshAllUserPort: function (t) {
        $.ajax({
            url: "xj/call",
            type: "post",
            dataType: "json",
            data: {jroute: "gangkou/listCreated"},
            success: function (e) {
                MAPutils.createPortFeatures(e, {portType: "user"}), t && t()
            }
        })
    },
    createPortFeatures: function (e, t) {
        e = _.isArray(e) ? e : [e];
        var i = $.extend({clearLayer: !0, portType: "nb"}, t);
        if (i.clearLayer) {
            var a = MAPutils.xj_portLayer.getSource(), r = a.getFeatures();
            _.forEach(r, function (e) {
                e.get("portType") == i.portType && a.removeFeature(e)
            })
        }
        for (var o = 0; o < e.length; o++) {
            var l = e[o], n = {
                geom: l.geom,
                id: l.pid,
                kv: {layerType: "xj_portLayer", data: l, id: l.pid, portType: i.portType},
                style: function (e, t) {
                    return 11 <= MAPutils.GlobalMap.getView().getZoom() ? new ol.style.Style({
                        fill: new ol.style.Fill({color: [67, 129, 209, .2]}),
                        stroke: new ol.style.Stroke({color: [67, 129, 209]}),
                        text: new ol.style.Text({
                            font: "bold 12px Microsoft YaHei",
                            text: e.get("data").names,
                            fill: new ol.style.Fill({color: [67, 129, 209]}),
                            stroke: new ol.style.Stroke({color: "#FFFFFF", width: 3})
                        })
                    }) : new ol.style.Style({
                        fill: new ol.style.Fill({color: [67, 129, 209, .2]}),
                        stroke: new ol.style.Stroke({color: [67, 129, 209]})
                    })
                }
            };
            MAPutils.drawVectorFeature(MAPutils.xj_portLayer, n)
        }
    },
    refreshWaterZone: function (t) {
        $.ajax({
            url: "xj/call",
            type: "post",
            data: {jroute: "waterzone/findAll"},
            dataType: "json",
            success: function (e) {
                MAPutils.xj_waterZoneLayer.getSource().clear(), e && e.length ? (_.forEach(e, function (e) {
                    MAPutils.drawVectorFeature(MAPutils.xj_waterZoneLayer, {
                        geom: e.geom,
                        id: e.id,
                        kv: {data: e},
                        style: new ol.style.Style({
                            fill: new ol.style.Fill({color: [46, 204, 113, .2]}),
                            stroke: new ol.style.Stroke({color: [46, 204, 113]}),
                            text: new ol.style.Text({
                                font: "bold 12px Microsoft YaHei",
                                text: e.name,
                                fill: new ol.style.Fill({color: [67, 129, 209]}),
                                stroke: new ol.style.Stroke({color: "#FFFFFF", width: 3})
                            })
                        })
                    })
                }), t && t()) : console.warn("重绘水域管理区，没有数据！ data=" + e)
            }
        })
    },
    refreshReportLine: function (t) {
        $.ajax({
            url: "xj/call",
            type: "post",
            dataType: "json",
            data: {jroute: "reportline/findAll"},
            success: function (e) {
                MAPutils.xj_ReportLineLayer.getSource().clear(), e && e.length ? (_.forEach(e, function (e) {
                    MAPutils.drawVectorFeature(MAPutils.xj_ReportLineLayer, {
                        geom: e.geom,
                        id: e.id,
                        kv: {data: e},
                        style: new ol.style.Style({
                            stroke: new ol.style.Stroke({color: [142, 68, 173], width: 2}),
                            text: new ol.style.Text({
                                font: "bold 12px Microsoft YaHei",
                                text: e.name,
                                fill: new ol.style.Fill({color: [67, 129, 209]}),
                                stroke: new ol.style.Stroke({color: "#FFFFFF", width: 3})
                            })
                        })
                    })
                }), t && t()) : console.warn("重绘报告线，没有数据！ data=" + e)
            }
        })
    },
    pointPop: function (i) {
        $.get("html/funcXj/mark/markPointPop.html", function (e) {
            var t = i.feature ? i.feature.get("data") : MAPutils.markLayer.getSource().getFeatureById(i.id).get("data");
            e = MAPutils.formatStr(e, {
                mark_name: t.mark_name,
                user_name: t.user_name,
                create_time: t.create_time,
                edit_time: t.edit_time,
                mark_info: t.mark_info || "无"
            }), layer.close(MAPutils.DialogIndex), MAPutils.DialogIndex = layer.open({
                type: 1,
                shade: 0,
                title: i.title,
                area: "300px",
                offset: i.offset,
                content: e
            })
        })
    },
    resetMap: function (e) {
        var t = e || WGAuth.getCenter();
        if (_.isArray(t)) {
            var i = t[0], a = t[1];
            if (!i || !a) return;
            try {
                var r = MAPutils.WKT.readGeometry(i).getCoordinates();
                MAPutils.GlobalMap.getView().animate({center: r, zoom: a, duration: 0})
            } catch (e) {
                console.error("复位地图 异常", e)
            }
        } else try {
            var o = MAPutils.WKT.readGeometry(t);
            MAPutils.GlobalMap.getView().fit(o, {size: MAPutils.GlobalMap.getSize(), constrainResolution: !1})
        } catch (e) {
            console.error("复位地图 异常", e)
        }
    },
    initVideoLayer: function (t) {
        var i, a;
        i = MAPutils.videoPlayLayer;
        try {
            a = CONFIG.video[t.name]
        } catch (t) {
            return void console.error("initVideoLayer初始化视频图层 参数异常", e)
        }
        _.forEach(a, function (e) {
            MAPutils.drawVectorFeature(i, {
                geom: e.geom,
                kv: {layerType: "videoPlayLayer"},
                style: new ol.style.Style({image: new ol.style.Icon({src: "image/video/map-cam.png"})})
            })
        })
    },
    destroyVideoLayer: function () {
        MAPutils.videoPlayLayer.getSource().clear()
    },
    openUserInfo: function () {
        $.get("html/user/user-info.html", function (e) {
            layer.open({type: 1, shade: 0, title: "用户角色管理", content: e, area: ["800px", "600px"]})
        })
    }
}, locatTo = function () {
    var t, a, r;
    return {
        "水文站": "swzLayer",
        "VHF基站": "radioLayer",
        "港口": "portLayer",
        "AIS基站": "aisLayer",
        "航标": "naviMarkLayer",
        "船舶管理": "shipLayer",
        "船舶信息": "shipLayer",
        "船舶轨迹": "shipLineLayer"
    }, t = {"船舶信息": "船舶", "船舶管理": "船舶"}, a = {
        "水文站": function (e) {
            MAPutils.locatTo(MAPutils.getCenterById(MAPutils.swzLayer, e))
        }, "VHF基站": function (e) {
            MAPutils.locatTo(MAPutils.getCenterById(MAPutils.radioLayer, e), 14, function () {
                MAPutils.radioPop(e, MAPutils.getPopOffSet())
            })
        }, "港口": function (e, t) {
            var i = MAPutils.portLayer.getSource().getFeatureById(e);
            i ? MAPutils.locatWithTwinkle(MAPutils.getFeatureCenter(i)) : MAPutils.initAllPort(function () {
                MAPutils.locatWithTwinkle(MAPutils.getCenterById(MAPutils.portLayer, e))
            })
        }, "AIS基站": function (e) {
            MAPutils.locatWithTwinkle(MAPutils.getCenterById(MAPutils.aisLayer, e), 14, function () {
                MAPutils.AISPop(e, MAPutils.getPopOffSet())
            })
        }, "航标": function (e) {
            MAPutils.locatTo(MAPutils.getCenterById(MAPutils.naviMarkLayer, e), 14, function () {
                MAPutils.naviMarkPop(e, MAPutils.getPopOffSet())
            })
        }, "船舶管理": function (e) {
            a["船舶信息"](e)
        }, "船舶信息": function (i, a) {
            var e = MAPutils.shipLayer.getSource().getFeatureById(i);
            e ? MAPutils.locatTo(MAPutils.getFeatureCenter(e), 14, function () {
                MAPutils.refreshShip(function () {
                    MAPutils.shipPop(i, MAPutils.getPopOffSet()), a && a.callback && a.callback()
                })
            }) : $.get("ship/getShipLocByMmsi", maphaoEncryptJson({mmsi: i}), function (e) {
                if (!e.result) return layer.msg("船舶当前未在线！", {icon: 5, anim: 6}), !1;
                if (!(e = maphaoDecryptJson(e.result)) || "false" == e || !e.geom) return layer.msg("船舶当前未在线！", {
                    icon: 5,
                    anim: 6
                }), !1;
                var t = MAPutils.geom2Coor(e.geom);
                MAPutils.locatTo(t, 14, function () {
                    MAPutils.refreshShip(function () {
                        MAPutils.shipPop(i, MAPutils.getPopOffSet()), a && a.callback && a.callback()
                    })
                })
            })
        }, "船舶轨迹": function (e, t) {
            var i = (new ol.format.WKT).readGeometry(t.geom, {
                dataProjection: "EPSG:4326",
                featureProjection: "EPSG:3857"
            }).getCoordinates();
            MAPutils.locatTo(i[Math.ceil(i.length / 2)], 18), MAPutils.FeatureAnim.start({
                id: t.id,
                animType: MAPutils.animType.shipLine,
                geom: t.geom,
                layerType: "shipLineLayer",
                maxScale: 1.6,
                minScale: 1,
                scaleStep: .01,
                twinkleTimes: 4,
                closeSameType: !0,
                styleFn: function (e) {
                    var t = [243, 0, 0, .8 * (1 - e.scalePercent)], i = new ol.style.Style({
                        stroke: new ol.style.Stroke({color: t}),
                        fill: new ol.style.Fill({color: t})
                    });
                    e.feature.setStyle(i)
                }
            })
        }
    }, r = function (e) {
        e = t[e] ? t[e] : e, LayerControl.switch(e, !0)
    }, function (e, t, i) {
        r(t), a[t] && a[t](e, i)
    }
}();
MAPutils.init();