(ns rtl-test
  "Restoration-fidelity tests — one per original kami-rtl Rust test
  (kami-engine/kami-rtl/src/lib.rs `mod tests`, deleted PR #82)."
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [rtl]
            [rtl.hdl :as hdl]
            [rtl.simulator :as sim]
            [rtl.waveform :as waveform]
            [rtl.synthesis :as synthesis]))

(deftest namespace-loads
  (testing "the restored CLJC namespace loads"
    (is (some? (the-ns 'rtl)))))

;; mirrors `test_parse_basic_verilog_module`
(deftest parse-basic-verilog-module
  (let [src "\nmodule counter(clk, rst, out);\n  // body elided\nendmodule\n"
        [status [name ports]] (hdl/parse-verilog src)]
    (is (= :ok status))
    (is (= "counter" name))
    (is (= ["clk" "rst" "out"] ports))))

;; mirrors `test_simulator_event_processing`
(deftest simulator-event-processing
  (let [s (-> (sim/simulator)
              (sim/register-signal "clk" 1)
              (sim/register-signal "data" 1)
              (sim/schedule-event 5 "clk" [:one])
              (sim/schedule-event 10 "data" [:one])
              (sim/schedule-event 15 "clk" [:zero])
              (sim/run 20))]
    (is (= 20 (:time s)))
    (is (= :zero (first (:values (sim/get-signal s "clk")))))
    (is (= :one (first (:values (sim/get-signal s "data")))))
    (let [clk-hist (sim/get-signal-history s "clk")]
      (is (= 2 (count clk-hist)))
      (is (= 5 (:time (nth clk-hist 0))))
      (is (= 15 (:time (nth clk-hist 1)))))))

;; mirrors `test_vcd_export_format`
(deftest vcd-export-format
  (let [signals [(waveform/waveform-signal
                  {:name "clk" :width 1
                   :transitions [[0 "0"] [5 "1"] [10 "0"]]
                   :display-format :binary :color "#00ff00"})]
        vcd (waveform/export-vcd signals)]
    (is (str/includes? vcd "$date"))
    (is (str/includes? vcd "$timescale"))
    (is (str/includes? vcd "$var wire 1"))
    (is (str/includes? vcd "$enddefinitions $end"))
    (is (str/includes? vcd "$dumpvars"))
    (is (str/includes? vcd "#0"))
    (is (str/includes? vcd "#5"))
    (is (str/includes? vcd "#10"))))

;; mirrors `test_logic_value_display`
(deftest logic-value-display
  (is (= "0" (sim/logic-value->str :zero)))
  (is (= "1" (sim/logic-value->str :one)))
  (is (= "x" (sim/logic-value->str :x)))
  (is (= "z" (sim/logic-value->str :z)))
  (is (= :zero (sim/logic-and :one :zero)))
  (is (= :one (sim/logic-or :one :zero)))
  (is (= :zero (sim/logic-not :one)))
  (is (= :x (sim/logic-and :x :one))))

;; mirrors `test_gate_netlist_stats_and_optimize`
(deftest gate-netlist-stats-and-optimize
  (let [netlist {:gates [(synthesis/gate :and ["1'b1" "1'b1"] "n1")
                          (synthesis/gate :or ["n1" "a"] "n2")
                          (synthesis/gate :dff ["n2"] "q")]
                 :primary-inputs ["a" "1'b0" "1'b1"]
                 :primary-outputs ["q"]}
        stats (synthesis/stats netlist)]
    (is (= 3 (:gate-count stats)))
    (is (= 1 (:ff-count stats)))
    (is (= 2 (:lut-count stats)))
    (let [optimized (synthesis/optimize netlist)
          gate-types (map :gate-type (:gates optimized))]
      (is (= 2 (count (:gates optimized))))
      (is (not (some #{:and} gate-types)))
      (is (some #{:or} gate-types))
      (is (some #{:dff} gate-types)))))
