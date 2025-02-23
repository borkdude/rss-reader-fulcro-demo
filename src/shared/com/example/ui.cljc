(ns com.example.ui
  (:require
    #?@(:cljs [[com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown :refer [ui-dropdown]]
               [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-menu :refer [ui-dropdown-menu]]
               [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-item :refer [ui-dropdown-item]]])
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [com.example.ui.account-forms :refer [AccountForm AccountList]]
    [com.example.ui.invoice-forms :refer [InvoiceForm InvoiceList AccountInvoices]]
    [com.example.ui.item-forms :refer [ItemForm InventoryReport]]
    [com.example.ui.line-item-forms :refer [LineItemForm]]
    [com.example.ui.login-dialog :refer [LoginForm]]
    [com.example.ui.sales-report :as sales-report]
    [com.example.ui.dashboard :as dashboard]
    [com.example.ui.master-detail :as mdetail]
    [com.example.ui.stories-forms :as stories]
    [com.example.ui.button-toys-form :as buttons]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.routing.dynamic-routing :refer [defrouter]]
    [com.fulcrologic.rad.authorization :as auth]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.routing :as rroute]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]))

(defsc LandingPage [this props]
  {:query         ['*]
   :ident         (fn [] [:component/id ::LandingPage])
   :initial-state {}
   :route-segment ["landing-page"]}
  (dom/div :.ui.basic.segment
    (dom/div :.ui.active.loader.text.loader
      "Welcome to the Demo. Please log in.")))



;; This will just be a normal router...but there can be many of them.
;    builds initial-state
(defrouter MainRouter [this {:keys [current-state route-factory route-props]}]
  {:always-render-body? true
   :router-targets      [LandingPage
                         stories/StoriesContainer
                          ItemForm InvoiceForm InvoiceList AccountList AccountForm AccountInvoices
                         sales-report/SalesReport InventoryReport
                         sales-report/RealSalesReport
                         dashboard/Dashboard
                         ;stories/StoriesListReport stories/StoryReport
                         stories/StoriesMain stories/StoriesSearch
                         stories/ModeTest1

                         buttons/ButtonTest1
                         buttons/ButtonTest1b
                         buttons/ButtonTest2
                         buttons/ButtonTest3
                         mdetail/AccountList]}
  ;; Normal Fulcro code to show a loader on slow route change (assuming Semantic UI here, should
  ;; be generalized for RAD so UI-specific code isn't necessary)
  (dom/div
    (dom/div :.ui.loader {:classes [(when-not (= :routed current-state) "active")]})
    (when route-factory
      (route-factory route-props))))

(def ui-main-router (comp/factory MainRouter))

(auth/defauthenticator Authenticator {:local LoginForm})

(def ui-authenticator (comp/factory Authenticator))

(defsc Root [this {
                   ;::auth/keys [authorization]
                   ::app/keys  [active-remotes]
                   :keys       [;authenticator
                                router]
                   :as props}]
  {:query         [;{:authenticator (comp/get-query Authenticator)}
                   {:router (comp/get-query MainRouter)}
                   ::app/active-remotes]
                   ;::auth/authorization]
   :initial-state {:router        {}}}
                   ; ^^^^ macro magic happening here: associating it with MainRounter, from the :query
                   ;:router        (comp/get-initial-state MainRouter {})
                   ;:authenticator {}}}
  (let [;logged-in? (= :success (some-> authorization :local ::auth/status))
        busy?      (seq active-remotes)]
        ;username   (some-> authorization :local :account/name)]
    (dom/div
      (div :.ui.top.menu
        (div :.ui.item "Demo")
        (when true ;logged-in?
          #?(:cljs
             (comp/fragment
               (ui-dropdown {:className "item" :text "Account"}
                 (ui-dropdown-menu {}
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this AccountList {}))} "View All")
                   (ui-dropdown-item {:onClick (fn [] (form/create! this AccountForm))} "New")))
               (ui-dropdown {:className "item" :text "Inventory"}
                 (ui-dropdown-menu {}
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this InventoryReport {}))} "View All")
                   (ui-dropdown-item {:onClick (fn [] (form/create! this ItemForm))} "New")))
               (ui-dropdown {:className "item" :text "Invoices"}
                 (ui-dropdown-menu {}
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this InvoiceList {}))} "View All")
                   (ui-dropdown-item {:onClick (fn [] (form/create! this InvoiceForm))} "New")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this AccountInvoices {:account/id (new-uuid 101)}))} "Invoices for Account 101")))
               (ui-dropdown {:className "item" :text "Reports"}
                 (ui-dropdown-menu {}
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this dashboard/Dashboard {}))} "Dashboard")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this sales-report/RealSalesReport {}))} "Sales Report")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this mdetail/AccountList {}))} "Master Detail")))
               (ui-dropdown {:className "item" :text "Button Toys"}
                 (ui-dropdown-menu {}
                   (ui-dropdown-item {:onClick (fn [] (dr/change-route! this ["button-test-1"] {}))} "Button Test 1")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this buttons/ButtonTest1b {}))} "Button Test 1b")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this buttons/ButtonTest2 {}))} "Button Test 2")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this buttons/ButtonTest3 {}))} "Button Test 3")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this buttons/ButtonTest3 {:ui/number2 (:ui/number2 props)}))} "Button Test 3a")))
               (ui-dropdown {:className "item" :text "Gene"}
                 (ui-dropdown-menu {}
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this stories/StoriesMain {}))} "Stories Main")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this stories/StoriesSearch {}))} "Stories Searched")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this stories/StoriesContainer {}))} "Stories Container")
                   (ui-dropdown-item {:onClick (fn [] (rroute/route-to! this stories/ModeTest1 {}))} "Mode Test"))))))


        (div :.right.menu
          (div :.item
            (div :.ui.tiny.loader {:classes [(when busy? "active")]})
            ent/nbsp ent/nbsp ent/nbsp ent/nbsp)
          (if true; logged-in?
            (comp/fragment
              (div :.ui.item
                (str "Logged in as " "hello")) ; username
              (div :.ui.item
                (dom/button :.ui.button {:onClick (fn []
                                                    ;; TODO: check if we can change routes...
                                                    (rroute/route-to! this stories/StoriesContainer {})
                                                    (auth/logout! this :local))}
                  "Logout")))
            (div :.ui.item))))
              ;(dom/button :.ui.primary.button {:onClick #(auth/authenticate! this :local nil)}
              ;  "Login")))))
      ;(div :.ui.container.segment
      (div :.ui.segment
        ;(ui-authenticator authenticator)
        (ui-main-router router)))))

(def ui-root (comp/factory Root))

