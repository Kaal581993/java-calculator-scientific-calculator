import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [expression, setExpression] = useState('');
  const [result, setResult] = useState('');

  const handleClick = (value) => {
    setExpression((prev) => prev + value);
  };

  const handleClear = () => {
    setExpression('');
    setResult('');
  };

  const handleCalculate = async () => {
    try {
      const res = await axios.post('http://localhost:8080/api/calc', {
        expression
      });
      setResult(res.data.result);
    } catch (err) {
      setResult("Error");
    }
  };

  const buttons = [
    "7", "8", "9", "/", "sqrt(", "fact(",
    "4", "5", "6", "*", "sin(", "cos(",
    "1", "2", "3", "-", "tan(", "log(",
    "0", ".", "(", ")", "+", "ln(",
    "exp(", "pow(", ")", "C", "="
  ];

  return (
    <div className="calculator">
      <h2>Scientific Calculator</h2>
      <input type="text" value={expression} readOnly />
      <div className="result">Result: {result}</div>
      <div className="buttons">
        {buttons.map((btn, idx) => (
          btn === "="
            ? <button key={idx} onClick={handleCalculate}>=</button>
            : btn === "C"
              ? <button key={idx} onClick={handleClear}>C</button>
              : <button key={idx} onClick={() => handleClick(btn)}>{btn}</button>
        ))}
      </div>
    </div>
  );
}

export default App;
