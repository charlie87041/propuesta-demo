#!/usr/bin/env node

/**
 * TDD Plan to CCPM PRD Converter
 * 
 * Converts a TDD implementation plan (from planner-tdd agent) into a CCPM-compatible PRD
 * that can be used with /pm commands to auto-generate GitHub issues.
 * 
 * Usage:
 *   node tdd-to-prd.js <plan-file> <output-prd-name>
 * 
 * Example:
 *   node tdd-to-prd.js user-auth-plan.md user-auth
 *   # Creates prds/user-auth.md ready for CCPM
 */

const fs = require('fs');
const path = require('path');

// Parse command line arguments
const args = process.argv.slice(2);
if (args.length < 2) {
  console.error('Usage: node tdd-to-prd.js <plan-file> <output-prd-name>');
  process.exit(1);
}

const planFile = args[0];
const prdName = args[1];

// Read plan file
let planContent;
try {
  planContent = fs.readFileSync(planFile, 'utf8');
} catch (err) {
  console.error(`Error reading plan file: ${err.message}`);
  process.exit(1);
}

// Parse the plan into structured data
function parsePlan(content) {
  const milestones = [];
  const lines = content.split('\n');
  
  let currentMilestone = null;
  let currentIssue = null;
  let currentSection = null;
  
  for (const line of lines) {
    // Milestone detection
    if (line.match(/^### Milestone \d+:/)) {
      if (currentMilestone) {
        if (currentIssue) {
          currentMilestone.issues.push(currentIssue);
        }
        milestones.push(currentMilestone);
      }
      
      const match = line.match(/^### Milestone (\d+): (.+?) \((.+?)\)/);
      currentMilestone = {
        number: match[1],
        name: match[2],
        duration: match[3],
        goal: '',
        properties: [],
        issues: []
      };
      currentIssue = null;
      currentSection = null;
      continue;
    }
    
    // Milestone goal
    if (line.match(/^\*\*Goal\*\*:/)) {
      currentMilestone.goal = line.replace(/^\*\*Goal\*\*:\s*/, '');
      continue;
    }
    
    // Milestone properties
    if (line.match(/^\*\*Properties to validate\*\*:/)) {
      currentSection = 'properties';
      continue;
    }
    
    if (currentSection === 'properties' && line.match(/^- Property \d+:/)) {
      currentMilestone.properties.push(line.replace(/^- /, ''));
      continue;
    }
    
    // Issue detection
    if (line.match(/^- Issue \d+\.\d+:/)) {
      if (currentIssue) {
        currentMilestone.issues.push(currentIssue);
      }
      
      const match = line.match(/^- Issue (\d+)\.(\d+): (.+?) \((.+?)\)/);
      currentIssue = {
        milestoneNum: match[1],
        issueNum: match[2],
        title: match[3],
        estimate: match[4],
        description: '',
        red: [],
        green: [],
        refactor: [],
        properties: [],
        dependencies: []
      };
      currentSection = 'description';
      continue;
    }
    
    // Issue sections
    if (!currentIssue) continue;
    
    if (line.match(/^\s+- Red:/)) {
      currentSection = 'red';
      continue;
    }
    if (line.match(/^\s+- Green:/)) {
      currentSection = 'green';
      continue;
    }
    if (line.match(/^\s+- Refactor:/)) {
      currentSection = 'refactor';
      continue;
    }
    if (line.match(/^\s+- Properties:/)) {
      currentSection = 'properties';
      continue;
    }
    if (line.match(/^\s+- Dependencies:/)) {
      currentSection = 'dependencies';
      continue;
    }
    
    // Collect content for current section
    if (currentSection === 'description' && line.trim()) {
      currentIssue.description += line.trim() + ' ';
    } else if (currentSection === 'red' && line.match(/^\s+- /)) {
      currentIssue.red.push(line.trim().replace(/^- /, ''));
    } else if (currentSection === 'green' && line.match(/^\s+- /)) {
      currentIssue.green.push(line.trim().replace(/^- /, ''));
    } else if (currentSection === 'refactor' && line.match(/^\s+- /)) {
      currentIssue.refactor.push(line.trim().replace(/^- /, ''));
    } else if (currentSection === 'properties' && line.match(/^\s+- Property/)) {
      currentIssue.properties.push(line.trim().replace(/^- /, ''));
    } else if (currentSection === 'dependencies' && line.match(/^\s+- /)) {
      currentIssue.dependencies.push(line.trim().replace(/^- /, ''));
    }
  }
  
  // Add last issue and milestone
  if (currentIssue) {
    currentMilestone.issues.push(currentIssue);
  }
  if (currentMilestone) {
    milestones.push(currentMilestone);
  }
  
  return milestones;
}

// Generate PRD content
function generatePRD(milestones, projectName) {
  let prd = `# ${projectName} PRD\n\n`;
  prd += `## Overview\n\n`;
  prd += `This PRD was auto-generated from a TDD implementation plan.\n`;
  prd += `All tasks follow the Red-Green-Refactor cycle with property-based testing.\n\n`;
  
  prd += `## Milestones\n\n`;
  
  for (const milestone of milestones) {
    prd += `### Milestone ${milestone.number}: ${milestone.name}\n\n`;
    prd += `**Duration**: ${milestone.duration}\n`;
    prd += `**Goal**: ${milestone.goal}\n\n`;
    
    if (milestone.properties.length > 0) {
      prd += `**Properties to validate**:\n`;
      for (const prop of milestone.properties) {
        prd += `- ${prop}\n`;
      }
      prd += `\n`;
    }
    
    prd += `#### Issues\n\n`;
    
    for (const issue of milestone.issues) {
      prd += `##### Issue ${issue.milestoneNum}.${issue.issueNum}: ${issue.title}\n\n`;
      prd += `**Labels**: enhancement, backend, testing\n`;
      prd += `**Estimate**: ${issue.estimate}\n\n`;
      
      prd += `**Description**:\n`;
      prd += `${issue.description.trim()}\n\n`;
      
      if (issue.properties.length > 0) {
        prd += `**Related Properties**:\n`;
        for (const prop of issue.properties) {
          prd += `- ${prop}\n`;
        }
        prd += `\n`;
      }
      
      prd += `**TDD Approach**:\n\n`;
      
      prd += `Red Phase (Write Failing Tests):\n`;
      for (const item of issue.red) {
        prd += `- [ ] ${item}\n`;
      }
      prd += `\n`;
      
      prd += `Green Phase (Minimum Implementation):\n`;
      for (const item of issue.green) {
        prd += `- [ ] ${item}\n`;
      }
      prd += `\n`;
      
      prd += `Refactor Phase (Improve & Document):\n`;
      for (const item of issue.refactor) {
        prd += `- [ ] ${item}\n`;
      }
      prd += `- [ ] Write Javadoc/JSDoc for all public methods\n`;
      prd += `- [ ] Add inline comments for complex logic\n`;
      prd += `- [ ] Update API documentation (if applicable)\n`;
      prd += `\n`;
      
      prd += `**Acceptance Criteria**:\n`;
      prd += `- [ ] All unit tests pass\n`;
      prd += `- [ ] All integration tests pass\n`;
      if (issue.properties.length > 0) {
        prd += `- [ ] Property tests pass (100+ iterations)\n`;
      }
      prd += `- [ ] Code coverage > 80%\n`;
      prd += `- [ ] No linter/compiler warnings\n`;
      prd += `- [ ] Inline documentation complete\n`;
      prd += `- [ ] API documentation updated (if applicable)\n`;
      prd += `- [ ] Code review approved\n`;
      prd += `- [ ] CI/CD pipeline green\n\n`;
      
      if (issue.dependencies.length > 0) {
        prd += `**Dependencies**:\n`;
        for (const dep of issue.dependencies) {
          prd += `- ${dep}\n`;
        }
        prd += `\n`;
      }
    }
  }
  
  return prd;
}

// Main execution
console.log('Parsing TDD plan...');
const milestones = parsePlan(planContent);

console.log(`Found ${milestones.length} milestones`);
for (const m of milestones) {
  console.log(`  - Milestone ${m.number}: ${m.name} (${m.issues.length} issues)`);
}

console.log('\nGenerating PRD...');
const prdContent = generatePRD(milestones, prdName);

// Create prds directory if it doesn't exist
const prdsDir = path.join(process.cwd(), 'prds');
if (!fs.existsSync(prdsDir)) {
  fs.mkdirSync(prdsDir, { recursive: true });
}

// Write PRD file
const prdPath = path.join(prdsDir, `${prdName}.md`);
fs.writeFileSync(prdPath, prdContent);

console.log(`\n✅ PRD created: ${prdPath}`);
console.log('\nNext steps:');
console.log(`1. Review and edit: ${prdPath}`);
console.log(`2. Generate issues: /pm:prd-new ${prdName}`);
console.log(`3. Parse PRD: /pm:prd-parse ${prdName}`);
console.log(`4. Create issues: /pm:epic-oneshot ${prdName}`);